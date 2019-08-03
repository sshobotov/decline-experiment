package tickets4sale.core

import java.io.{File, InputStream}

import scala.language.higherKinds

object Files {
  def resource(path: String) = new File(getClass.getClassLoader.getResource(path).getFile)

  def resourceStream(path: String): InputStream = getClass.getClassLoader.getResourceAsStream(path)
}

