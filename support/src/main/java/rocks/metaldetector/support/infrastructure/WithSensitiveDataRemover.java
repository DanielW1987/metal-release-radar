package rocks.metaldetector.support.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public interface WithSensitiveDataRemover {

  String REMOVED_FOR_LOGGING_STRING = "REMOVED_FOR_LOGGING";
  String PAYLOAD_IDENTIFIER = "payload={";
  List<String> SENSITIVE_DATA_FIELD_NAME = List.of(
          "plainPassword",
          "verifyPlainPassword",
          "code",
          "token"
  );

  default String removeTokenFromHeader(String headerString) {
    if (headerString != null && headerString.contains(AUTHORIZATION)) {
      int startIndex = headerString.indexOf(AUTHORIZATION) + AUTHORIZATION.length() + 2;
      int endIndex = headerString.indexOf('"', startIndex);
      headerString = new StringBuilder(headerString).replace(startIndex, endIndex, REMOVED_FOR_LOGGING_STRING).toString();
    }

    return headerString;
  }

  default String removeSensitiveDataFromPayload(String originalMessage) throws JsonProcessingException {
    if (originalMessage != null && originalMessage.toLowerCase().contains(PAYLOAD_IDENTIFIER)) {
      String prePayload = originalMessage.substring(0, originalMessage.indexOf(PAYLOAD_IDENTIFIER) + PAYLOAD_IDENTIFIER.length() - 1);
      String payload = originalMessage.substring(originalMessage.indexOf(PAYLOAD_IDENTIFIER) + PAYLOAD_IDENTIFIER.length() - 1);

      ObjectMapper mapper = new ObjectMapper();
      TypeReference<LinkedHashMap<String, String>> typeRef = new TypeReference<>() {};
      Map<String, String> payloadAsMap = mapper.readValue(payload, typeRef);
      SENSITIVE_DATA_FIELD_NAME.forEach(fieldName -> {
        if (payloadAsMap.containsKey(fieldName)) {
          payloadAsMap.put(fieldName, REMOVED_FOR_LOGGING_STRING);
        }
      });

      payload = mapper.writeValueAsString(payloadAsMap);

      return prePayload.concat(payload);
    }

    return originalMessage;
  }
}
