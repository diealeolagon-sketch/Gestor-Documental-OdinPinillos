package com.odin.odin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class OdinApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(OdinApplication.class, args);
	}

	private static void loadDotEnv() {
		Path[] candidates = new Path[] {
				Paths.get(".env"),
				Paths.get("Odin", ".env"),
				Paths.get(System.getProperty("user.dir", "."), ".env"),
				Paths.get(System.getProperty("user.dir", "."), "Odin", ".env")
		};

		Path envFile = null;
		for (Path p : candidates) {
			if (Files.isRegularFile(p)) {
				envFile = p.toAbsolutePath().normalize();
				break;
			}
		}
		if (envFile == null) {
			System.out.println("[ODIN] No se encontró archivo .env — use variables de entorno del sistema o Run Configuration.");
			return;
		}

		int loaded = 0;
		try (BufferedReader reader = Files.newBufferedReader(envFile, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				int eq = line.indexOf('=');
				if (eq <= 0) {
					continue;
				}
				String key = line.substring(0, eq).trim();
				String value = line.substring(eq + 1).trim();
				if ((value.startsWith("\"") && value.endsWith("\""))
						|| (value.startsWith("'") && value.endsWith("'"))) {
					value = value.substring(1, value.length() - 1);
				}
				// Solo si no está ya definida en el entorno del SO / IDE
				if (System.getenv(key) == null && System.getProperty(key) == null) {
					System.setProperty(key, value);
					loaded++;
				}
			}
			System.out.println("[ODIN] .env cargado desde " + envFile + " (" + loaded + " variables).");
		} catch (Exception e) {
			System.err.println("[ODIN] No se pudo leer .env: " + e.getMessage());
		}
	}
}
