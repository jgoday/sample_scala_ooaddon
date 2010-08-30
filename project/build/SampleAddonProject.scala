import sbt._

class SampleAddonProject(info: ProjectInfo) extends DefaultProject(info) {
    val sdk = "file:///usr/lib/openoffice-dev"
    val basis = sdk + "/basis3.4/program/classes/"
    val ure = sdk + "/ure/share/java/"


    val juh      = "oo" % "juh" % "3.4" from "%s/juh.jar".format(ure)
    val java_uno = "oo" % "java_uno" % "3.4" from "%s/java_uno.jar".format(ure)
    val ridl     = "oo" % "ridl" % "3.4" from "%s/ridl.jar".format(ure)
    val jurt     = "oo" % "jurt" % "3.4" from "%s/jurt.jar".format(ure)
    val uno_load = "oo" % "uno_loader" % "3.4" from "%s/unoloader.jar".format(ure)
    val unoil    = "oo" % "unoil" % "3.4" from "%s/unoil.jar".format(basis)

    override def packageOptions = ManifestAttributes(
                ("RegistrationClassName", "sample.oo.SampleAddonFactory"),
                ("Class-Path", "scala-library.jar")
            ) :: Nil

    lazy val create_extension = task
    {
        def _copy(from: Path, to: Path) =
            FileUtilities.copyFile(from, to, logImpl)

        println(propertyNames)

        FileUtilities.doInTemporaryDirectory(logImpl) (
            temp => {
                val _temp = temp.getName
                val _projectLib = path(jarPath.name)
                val _name = (projectName.get getOrElse "default") + ".oxt"

                val _files = "Addons.xcu" :: "ProtocolHandler.xcu" :: "META-INF" :: "description.xml" ::
                             "scala-library.jar" :: jarPath.name :: Nil

                _copy(jarPath, _projectLib)
                FileUtilities.createDirectory(path("META-INF"), logImpl)
                _copy(path("manifest.xml"), "META-INF" / "manifest.xml")
                _copy(mainDependencies.scalaLibrary.get.elements.next, "scala-library.jar")

                FileUtilities.zip(_files.map(path(_)), "target" / _name, true, logImpl)

                log.info("Create oxt file in target/" + _name)

                FileUtilities.clean(path("META-INF"), logImpl)
                FileUtilities.clean(path("scala-library.jar"), logImpl)
                FileUtilities.clean(_projectLib, logImpl)
                FileUtilities.clean(path(temp.getName), logImpl)

                Right("ok")
            }
        )

        None
    }
}