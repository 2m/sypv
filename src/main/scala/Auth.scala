import com.google.api.client.auth.oauth2.StoredCredential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.{ GoogleAuthorizationCodeFlow, GoogleClientSecrets }
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.InputStreamReader;

import scala.collection.JavaConversions._

object Auth {

  val JsonFactory = new JacksonFactory()
  val HttpTransport = new NetHttpTransport()
  val CredentialsDirectory = ".oauth-credentials"

  def authorize(scopes: List[String], credentialDatastore: String) = {
    // Load client secrets.
    val clientSecretReader = new InputStreamReader(getClass.getResourceAsStream("/client_secrets.json"))
    val clientSecrets = GoogleClientSecrets.load(JsonFactory, clientSecretReader)

    // Checks that the defaults have been replaced (Default = "Enter X here").
    if (clientSecrets.getDetails.getClientId.startsWith("Enter") || clientSecrets.getDetails.getClientSecret.startsWith("Enter")) {
      println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=youtube into src/main/resources/client_secrets.json")
      System.exit(1)
    }

    // This creates the credentials datastore at ~/.oauth-credentials/${credentialDatastore}
    val fileDataStoreFactory = new FileDataStoreFactory(new File(System.getProperty("user.home") + "/" + CredentialsDirectory))
    val datastore = fileDataStoreFactory.getDataStore[StoredCredential](credentialDatastore)

    val flow = new GoogleAuthorizationCodeFlow.Builder(HttpTransport, JsonFactory, clientSecrets, scopes)
      .setCredentialDataStore(datastore)
      .build()

    // Build the local server and bind it to port 8080
    val localReceiver = new LocalServerReceiver.Builder().setPort(8080).build();

    // Authorize.
    new AuthorizationCodeInstalledApp(flow, localReceiver).authorize("user")
  }

}
