import java.io.File

import akka.testkit.TestKitBase
import org.apache.commons.io.FileUtils

import scala.util.Try

trait PersistenceCleanupSupport { this : TestKitBase =>
  private val storageLocations = List(
    "akka.persistence.journal.leveldb.dir",
    "akka.persistence.journal.leveldb-shared.store.dir",
    "akka.persistence.snapshot-store.local.dir",
  )
  protected def deleteStorageFiles(): Unit = {
    storageLocations map { location =>
      new File(system.settings.config.getString(location))
    } foreach { file =>
      Try(FileUtils.deleteDirectory(file))
    }
  }
}
