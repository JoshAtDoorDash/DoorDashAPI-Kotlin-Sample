import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.Key
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.HashMap

fun main() {
    val claims = HashMap<String, Any?>()

    // Populate claims used that will be used in JWT Payload
    claims["aud"] = "doordash"
    claims["iss"] = "UPDATE_WITH_DEVELOPER_ID" // TODO: Update value with Developer ID
    claims["kid"] = "UPDATE_WITH_KEY_ID" // TODO: Update value with Key ID
    claims["exp"] = ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(30).toEpochSecond()
    claims["iat"] = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond()

    // Signing Secret is Base64Encoded when generated on the Credentials page, need to decode to use
    val keyBytes = Decoders.BASE64URL.decode("UPDATE_WITH_SIGNING_SECRET") // TODO: Update value with Signing Secret
    val key: Key = Keys.hmacShaKeyFor(keyBytes)

    // Create JWT
    val jwt: String = Jwts.builder()
        .setHeaderParam("dd-ver", "DD-JWT-V1")
        .setHeaderParam("typ", "JWT")
        .setClaims(claims)
        .signWith(key)
        .compact()

    // Write the DoorDash API JWT
    println("DoorDash API JWT: $jwt")

    // Generate Unique ID for Delivery
    val deliveryId = UUID.randomUUID(); // TODO: Replace with generated system ID

    // Create data needed to create a new delivery
    val body = """{
    "external_delivery_id": "$deliveryId",
    "pickup_address": "901 Market Street 6th Floor San Francisco, CA 94103",
    "pickup_business_name": "Wells Fargo SF Downtown",
    "pickup_phone_number": "+16505555555",
    "pickup_instructions": "Enter gate code 1234 on the callbox.",
    "dropoff_address": "901 Market Street 6th Floor San Francisco, CA 94103",
    "dropoff_business_name": "Wells Fargo SF Downtown",
    "dropoff_phone_number": "+16505555555",
    "dropoff_instructions": "Enter gate code 1234 on the callbox.",
    "order_value": 1999
    }"""

    val client = HttpClient.newBuilder().build();
    val request = HttpRequest.newBuilder()
        .uri(URI.create("https://openapi.doordash.com/drive/v2/deliveries/"))
        .header("Authorization", "Bearer $jwt")
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // Write response body for create new delivery call
    println(response.body())
}
