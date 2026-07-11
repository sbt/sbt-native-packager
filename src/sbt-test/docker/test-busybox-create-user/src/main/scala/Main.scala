import com.sun.security.auth.module.UnixSystem

object Main {
  def main(args: Array[String]): Unit =
    println(System.getProperty("user.name"))
}
