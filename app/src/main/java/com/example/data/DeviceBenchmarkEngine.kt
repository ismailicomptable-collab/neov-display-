package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class DeviceBenchmarkEngine(private val context: Context) {

  suspend fun runLiveBenchmark(onProgress: (Float) -> Unit): DeviceBenchmarkResult =
    withContext(Dispatchers.Default) {
      val totalStartNs = System.nanoTime()
      onProgress(0.10f)

      // 1. Single-Core CPU Compute (Primes + Floating Point Trig)
      val scStart = System.nanoTime()
      val scWork = runCpuKernel(iterations = 180_000)
      val scElapsedMs = ((System.nanoTime() - scStart) / 1_000_000.0).coerceAtLeast(1.0)
      // Reference calibrated so modern flagship ~2500-3500, mid-range ~1100-1800
      val singleCoreScore = ((320_000.0 / scElapsedMs) * 1.45).toInt().coerceIn(250, 5000) +
        (scWork % 7)
      onProgress(0.40f)

      // 2. Multi-Core CPU Compute across all available cores
      val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
      val mcStart = System.nanoTime()
      coroutineScope {
        val jobs =
          (0 until cores).map {
            async(Dispatchers.Default) { runCpuKernel(iterations = 140_000) }
          }
        jobs.forEach { it.await() }
      }
      val mcElapsedMs = ((System.nanoTime() - mcStart) / 1_000_000.0).coerceAtLeast(1.0)
      val multiCoreScore =
        ((260_000.0 * cores / mcElapsedMs) * 1.35).toInt().coerceIn(singleCoreScore, 14000)
      onProgress(0.70f)

      // 3. RAM Copy / Allocation Bandwidth
      val memStart = System.nanoTime()
      val blockSize = 4 * 1024 * 1024 // 4 MB
      val src = ByteArray(blockSize) { (it and 0xFF).toByte() }
      val dst = ByteArray(blockSize)
      repeat(18) {
        System.arraycopy(src, 0, dst, 0, blockSize)
      }
      val memElapsedSec = ((System.nanoTime() - memStart) / 1_000_000_000.0).coerceAtLeast(0.001)
      val totalMbCopied = (18 * 4).toDouble()
      val memBandwidthMbPerSec = (totalMbCopied / memElapsedSec).toInt().coerceIn(100, 60000)
      onProgress(0.88f)

      // 4. Local Storage Sequential Write/Read Throughput
      val storageSpeedMbPerSec = measureStorageSpeedMbPerSec()
      onProgress(1.0f)

      val totalDurationMs = (System.nanoTime() - totalStartNs) / 1_000_000L
      val overall =
        ((singleCoreScore * 165.0) +
          (multiCoreScore * 145.0) +
          (memBandwidthMbPerSec * 12.0) +
          (storageSpeedMbPerSec * 45.0))
          .toInt()
          .coerceAtLeast(150_000)

      DeviceBenchmarkResult(
        singleCoreScore = singleCoreScore,
        multiCoreScore = multiCoreScore,
        memoryBandwidthMbPerSec = memBandwidthMbPerSec,
        storageSpeedMbPerSec = storageSpeedMbPerSec,
        estimatedOverallScore = overall,
        durationMs = totalDurationMs,
      )
    }

  private fun runCpuKernel(iterations: Int): Int {
    var primes = 0
    var acc = 0.0
    var n = 2
    var count = 0
    while (count < iterations) {
      if (n <= 9000) {
        var isPrime = true
        var d = 2
        while (d * d <= n) {
          if (n % d == 0) {
            isPrime = false
            break
          }
          d++
        }
        if (isPrime) primes++
      }
      val angle = (count and 255) * 0.0174532925
      acc += sin(angle) * cos(angle) + sqrt((count + 1).toDouble()) * 0.0001
      n++
      count++
    }
    return primes + acc.toInt()
  }

  private fun measureStorageSpeedMbPerSec(): Int {
    return runCatching {
      val tmpFile = File(context.cacheDir, "cominitech_bench.tmp")
      val chunk = ByteArray(512 * 1024) { 0x5A }
      val totalMb = 8.0 // 8 MB write + read
      val startNs = System.nanoTime()
      tmpFile.outputStream().use { out ->
        repeat(16) { out.write(chunk) }
        out.fd.sync()
      }
      tmpFile.inputStream().use { input ->
        while (input.read(chunk) > 0) {
          // consume
        }
      }
      tmpFile.delete()
      val elapsedSec = ((System.nanoTime() - startNs) / 1_000_000_000.0).coerceAtLeast(0.002)
      ((totalMb * 2.0) / elapsedSec).toInt().coerceIn(20, 4500)
    }.getOrDefault(350)
  }
}
