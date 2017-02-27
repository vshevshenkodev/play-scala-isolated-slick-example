package controller

import com.google.inject.Inject
import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.jdbc.DriverDataSource
import org.scalatestplus.play.FakeApplicationFactory
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{Binding, Module}
import play.api.{Application, Configuration, Environment}

/**
 * Set up an application factory that runs flyways migrations on in memory database.
 */
trait MyApplicationFactory extends FakeApplicationFactory {
  def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .configure(Map("myapp.database.url" -> "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"))
      .bindings(new FlywayModule)
      .build()
  }
}

class FlywayModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[FlywayMigrator].toSelf.eagerly() )
  }
}

class FlywayMigrator @Inject()(env: Environment, configuration: Configuration) {
  def onStart(): Unit = {
    val driver = configuration.underlying.getString("myapp.database.driver")
    val url = configuration.underlying.getString("myapp.database.url")
    val user = configuration.underlying.getString("myapp.database.user")
    val password = configuration.underlying.getString("myapp.database.password")
    val flyway = new Flyway
    flyway.setDataSource(new DriverDataSource(env.classLoader, driver, url, user, password))
    flyway.setLocations("filesystem:modules/flyway/src/main/resources/db/migration")
    flyway.migrate()
  }

  onStart()
}
