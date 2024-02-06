import database.DatabaseManager

import java.io.{File, IOException}
import java.nio.file.Path
import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet, SQLException, Statement}
import jakarta.xml.bind.{JAXBContext, JAXBException, Unmarshaller}
import xmlmodels.Company

class BatchXmlImporter {
  private val databaseManager: DatabaseManager = new DatabaseManager("jdbc:postgresql://127.0.0.1:5432/postgres", "postgres", "postgres")

  @throws[IOException]
  @throws[JAXBException]
  @throws[SQLException]
  def importFiles(folderPath: Path): Unit = {
    val paths: List[File] = listXmlFileFromFolders(folderPath)
    val companies: List[Company] = unmarshalXmlIntoCompanies(paths)
    databaseManager.insertCompaniesAndStaffIntoDatabase(companies)
  }

  private def listXmlFileFromFolders(folderPath: Path) = {
    // Get all XML files from the folder
    val fileExtension: String = ".xml"
    val paths: List[File] = listFiles(fileExtension)(folderPath.toFile).toList
    paths
  }

  private def unmarshalXmlIntoCompanies(paths: List[File]) = {
    // Unmarshal the XML files into Company objects
    val companies: List[Company] = for {
      path <- paths
    } yield {
      val file: File = new File(path.toString)
      val jaxbContext: JAXBContext = JAXBContext.newInstance(classOf[Company])
      val jaxbUnmarshaller: Unmarshaller = jaxbContext.createUnmarshaller
      jaxbUnmarshaller.unmarshal(file).asInstanceOf[Company]
    }
    companies
  }

  private def listFiles(fileExtension: String)(file: File): Array[File] = {
    val these: Array[File] = file.listFiles()
    these ++ these.filter(_.isFile).filter(_.toString.endsWith(fileExtension)).flatMap(listFiles(fileExtension))
  }
}
