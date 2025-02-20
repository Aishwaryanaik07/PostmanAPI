package com.manav.hashgenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class DestinationHashGenerator {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar DestinationHashGenerator.jar <PRN> <JSON File Path>");
            return;
        }

        String prn = args[0].toLowerCase().replaceAll("\\s", ""); // Convert PRN to lowercase and remove spaces
        String jsonFilePath = args[1];

        try {
            File jsonFile = new File(jsonFilePath);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonFile);

            String destinationValue = findDestination(rootNode);
            if (destinationValue == null) {
                System.out.println("Error: 'destination' key not found in the JSON file.");
                return;
            }

            String randomString = generateRandomString(8);
            String concatenatedString = prn + destinationValue + randomString;
            String md5Hash = generateMD5Hash(concatenatedString);

            System.out.println(md5Hash + ";" + randomString);
        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
        }
    }

    private static String findDestination(JsonNode node) {
        if (node.isObject()) {
            for (JsonNode value : node) {
                if (value.has("destination")) {
                    return value.get("destination").asText();
                }
                String result = findDestination(value);
                if (result != null) return result;
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                String result = findDestination(element);
                if (result != null) return result;
            }
        }
        return null;
    }

    private static String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error: MD5 algorithm not found.");
        }
    }
}
