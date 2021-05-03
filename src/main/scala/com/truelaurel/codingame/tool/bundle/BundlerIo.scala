package com.truelaurel.codingame.tool.bundle

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Paths, FileVisitOption}
import java.util.Objects

import scala.io.Source
import scala.util.control.NonFatal

trait BundlerIo {
  def readFile(file: File): List[String]

  def findFolder(packageElements: Array[String]): File

  def save(fileName: String, content: String): Unit

  def findFile(fileName: String): File

  def filesInFolder(folder: File): List[File]
}

case class StdBundlerIo(srcFolder: String = "./src/main/scala")
    extends BundlerIo {

  def readFile(file: File): List[String] = {
    println(s"reading from $file")
    try {
      Source.fromFile(file).getLines().toList
    } catch {
      case NonFatal(e) =>
        println("Error while reading file " + file)
        e.printStackTrace()
        throw e
    }
  }

  def findFolder(packageElements: Array[String]): File = {
    packageElements.foldLeft(new File(srcFolder)) {
      case (folder, pkg) =>
        val f = new File(folder, pkg)
        //TODO : could only import files listed in { cl1, cl2 }
        if (f.isDirectory) f else folder
    }
  }

  def save(fileName: String, content: String): Unit = {
    val targetFolder: String = "./target"
    val destFile = new File(targetFolder, fileName)
    val destFolder = destFile.getParentFile.toPath
    println(s"creating directory $destFolder")
    Files.createDirectories(destFolder)
    val pw = new PrintWriter(destFile)
    println(s"save $fileName")
    try {
      println(s"writing to $destFile")
      pw.write(content)
    } finally pw.close()

  }

  def findFile(fileName: String): File = {
    Files
      .find(
        Paths.get("src"),
        Int.MaxValue,
        (path, _) => path.endsWith(fileName),
        FileVisitOption.FOLLOW_LINKS
      )
      .findAny()
      .orElseThrow(() => new IllegalArgumentException(s"$fileName not found"))
      .toFile
  }

  def filesInFolder(folder: File): List[File] = {
    Objects.requireNonNull(folder, "Folder should not be null")
    val files =
      folder.listFiles((pathname: File) => !pathname.getName.startsWith("."))
    Objects.requireNonNull(
      files,
      "visibleFiles should not be null in folder " + folder
    )
    files.filterNot(_.isDirectory).toList.sortBy(_.getAbsolutePath)
  }
}
