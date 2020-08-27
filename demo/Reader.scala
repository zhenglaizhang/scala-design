import java.io._
import java.rmi.RemoteException

class Reader(fname: String) {
  private val in = new BufferedReader(new FileReader(fname))

  @throws(classOf[IOException])
  @throws(classOf[RemoteException])
  def read() = in.read()
}
