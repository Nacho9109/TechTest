package exercise2;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.BrowserType.LaunchOptions;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

public class QubikaSportsClub {

	Playwright playwright;
	APIRequest request;
	APIRequestContext requestContext;
	Browser browser;
	Page page;

	@BeforeTest
	public void setup() {
		playwright = Playwright.create();
		request = playwright.request();
		requestContext = request.newContext();
	}

	@Test
	public void registerUserApi() throws IOException {

		// Getting Request Body
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("email", "qatest6@qubika.com");
		data.put("password", "123456789");

		// POST Call: Register user. Request url, headers and body
		APIResponse apiPostResponse = requestContext
				.post("https://api.club-administration.qa.qubika.com/api/auth/register", RequestOptions.create()
						.setHeader("accept", "*/*").setHeader("Content-Type", "application/json").setData(data));

		// Validating response status code and text
		System.out.println("Response status code is " + apiPostResponse.status());
		Assert.assertEquals(apiPostResponse.status(), 201);
		Assert.assertEquals(apiPostResponse.statusText(), "Created");
		System.out.println(apiPostResponse.text());

		// JSON Response
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode postJsonResponse = objectMapper.readTree(apiPostResponse.body());
		System.out.println(postJsonResponse.toPrettyString());

	}

	@Test
	public void loginValidation() {
		
		browser = playwright.chromium().launch(new LaunchOptions().setHeadless(false));
		page = browser.newPage();

		// Navigate to Qubika Sports Club Management System
		page.navigate("https://club-administration.qa.qubika.com/#/auth/login");

		// Validate the login page is displayed
		assertThat(page).hasTitle("Qubika Club");

		// login with created user. Enter credentials and click on button
		page.getByPlaceholder("Usuario o correo electrónico").pressSequentially("qatest6@qubika.com");
		page.getByPlaceholder("Contraseña").pressSequentially("123456789");
		page.locator("//button[@type='submit']").click();

		// Validate the user is logged In
		assertThat(page).hasURL("https://club-administration.qa.qubika.com/#/dashboard");

		// Create Category
		
		page.locator("//a[contains(.,'Tipos de Categorias')]").click();
		page.locator("//button[contains(.,'Adicionar')]").click();
		page.locator("//input[@id='input-username']").pressSequentially("QACAT01");
		page.locator("//button[@type='submit']").click();
		
		//Create sub category and validation
		
		page.locator("//button[contains(.,'Adicionar')]").click();
		page.locator("//input[@id='input-username']").pressSequentially("QACAT02");
		page.locator("//span[@class='text-muted']").click();
		page.locator("//div[@role='combobox']").pressSequentially("KM49");
		page.keyboard().press("Enter");
		page.locator("//button[@type='submit']").click();
		
		// Validate category and sub-category were created
		
		page.locator("//a[contains(.,'143')]").click();
		Locator lastRow = page.locator(".table-responsive tbody tr:last-child");
		String catRowName = lastRow.locator("td:nth-child(1)").textContent();
		assertEquals(catRowName, "QACAT01", "Category was not found");
		String sucCatRowName = lastRow.locator("td:nth-child(2)").textContent();
		assertEquals(sucCatRowName, "KM49", "Subcategory was not found");
		
	}

	@AfterTest
	public void tearDown() {
		page.close();
		browser.close();
		playwright.close();
	}

}
