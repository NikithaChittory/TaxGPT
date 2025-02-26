package taxgpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Fully Automated TaxGPT: A tax assistant for Indian Income Tax Slabs.
 */
public class TaxGPT {
    private static String sumMessages = "";
    private static HashMap<String, String> userInformation = new HashMap<>();
    private static Graph taxFormGraph = new Graph(); // Graph to model tax form dependencies

    public static void main(String[] args) throws InterruptedException, IOException {
        initializeTaxFormGraph(); // Initialize the graph with tax form dependencies
        Scanner scanner = new Scanner(System.in);
        sumMessages = "Hi TaxGPT, you are an assistant who helps users navigate the tax filing process. The chatbot should be able to answer questions, provide guidance on filling out the tax form, and offer suggestions for deductions or credits the user may be eligible for. The chatbot should use natural language processing to understand user queries and respond in a conversational way. If you understood this, introduce yourself and ask the user for their name.";
        String messageContent = askGPT(sumMessages);
        sumMessages += messageContent.replace("\n\n", "");
        System.out.println("TaxGPT: " + messageContent + "\n----------------------------------");

        while (true) {
            System.out.print("User: ");
            String input = scanner.nextLine().replaceAll("\n", "");
            System.out.println("----------------------------------");
            if (input.toLowerCase().contains("bye")) {
                break;
            }

            String askPrompt = "Previous context: " + sumMessages.replaceAll("\n", "")
                    + " ||| Previous prompt: " + messageContent.replaceAll("\n", "")
                    + " ||| New prompt: "
                    + input.replaceAll("\n", "")
                    + "Only respond with at most 1 short sentence. Remember to ask the user for their details as you will use them to determine which forms they should fill out and also don't reintroduce yourself anymore. If the user asks a question, you should respond with 1 paragraph only. Be very clear and concise with your questions and responses."
                            .replaceAll("\\\\", "'");
            messageContent = askGPT(askPrompt);

            System.out.println("TaxGPT: " + messageContent + "\n----------------------------------");
            sumMessages += input.replace("\n\n", "") + messageContent.replace("\n\n", "");
            summarizeUserInformation(input);

            // Perform tax calculations and provide recommendations
            if (userInformation.containsKey("filingStatus") && userInformation.containsKey("income")) {
                double income = Double.parseDouble(userInformation.get("income"));
                String filingStatus = userInformation.get("filingStatus");

                // Calculate tax liability
                double taxLiability = TaxCalculator.calculateTax(income, filingStatus);
                System.out.println("TaxGPT: Your estimated tax liability is: ₹" + taxLiability);

                // Recommend deductions
                String deductions = DeductionIdentifier.identifyDeductions(userInformation);
                System.out.println("TaxGPT: You may be eligible for the following deductions: " + deductions);

                // Recommend tax forms
                String recommendedForm = determineTaxForm(userInformation);
                System.out.println("TaxGPT: Based on your information, you should fill out: " + recommendedForm);
                System.out.println("TaxGPT: Here are the dependent forms you may need: " + getDependentForms(recommendedForm));

                // Auto-populate and e-file tax forms
                if (userInformation.containsKey("autoFile") && userInformation.get("autoFile").equals("true")) {
                    String eFileResponse = eFileTaxForms(userInformation);
                    System.out.println("TaxGPT: " + eFileResponse);
                }
            }
        }

        scanner.close();
        System.out.println(askGPT(
                "Format this information onto a table (title and headers should be very descriptive of the data) that will allow the user to easily and quickly fill out their tax forms: "
                        + userInformation.toString()));
        System.out.println(
                "\n----------------------------------\nTaxGPT: Bye! See you next year :)\n----------------------------------");
    }

    /**
     * Initialize the tax form dependency graph.
     */
    private static void initializeTaxFormGraph() {
        taxFormGraph.addEdge("ITR-1", "Form 16"); // ITR-1 depends on Form 16 for salaried individuals
        taxFormGraph.addEdge("ITR-2", "Form 16"); // ITR-2 depends on Form 16 for individuals with capital gains
        taxFormGraph.addEdge("ITR-3", "Form 16"); // ITR-3 depends on Form 16 for business income
    }

    /**
     * Determine the tax form based on user information using a decision tree.
     */
    private static String determineTaxForm(HashMap<String, String> userInfo) {
        String filingStatus = userInfo.get("filingStatus");
        double income = Double.parseDouble(userInfo.get("income"));
        boolean hasInvestments = Boolean.parseBoolean(userInfo.getOrDefault("hasInvestments", "false"));

        if (filingStatus.equals("Individual")) {
            if (income <= 500000) {
                return "ITR-1";
            } else if (hasInvestments) {
                return "ITR-2";
            } else {
                return "ITR-1";
            }
        } else if (filingStatus.equals("HUF")) {
            return "ITR-2";
        }
        return "ITR-1"; // Default form
    }

    /**
     * Get dependent forms using graph traversal (BFS).
     */
    private static String getDependentForms(String form) {
        return taxFormGraph.bfs(form).toString();
    }

    /**
     * Summarize user information into a HashMap.
     */
    private static void summarizeUserInformation(String input) {
        String summarizedInfo = askGPT(
                "Summarize this conversation into a Java dictionary (format: { 'key1' : 'value1', 'key2' : 'value2' }) containing the most useful information related to taxes. Replace key1, key2, etc with var names. Separate key and value with : not =. Keep in mind that Java code should be able to parse this dictionary and store it into an actual dictionary so don't include lists ([]).: "
                        + sumMessages.replaceAll("\n", ""))
                .replaceAll("\n", "");
        if (summarizedInfo.contains("{")) {
            summarizedInfo = summarizedInfo.substring(summarizedInfo.indexOf("{") + 1, summarizedInfo.indexOf("}"));
        }
        String[] pairs = summarizedInfo.replaceAll("[\"'\n]", " ").split(" , ");

        for (String pair : pairs) {
            String[] keyValue = pair.split(" : ");
            if (keyValue.length == 2) {
                String key = keyValue[0].strip().replaceAll("'", "");
                String value = keyValue[1].strip().replaceAll("'", "");
                userInformation.put(key, value);
            }
        }
    }

    /**
     * Graph class to model tax form dependencies.
     */
    static class Graph {
        private Map<String, LinkedList<String>> adjList = new HashMap<>();

        public void addEdge(String src, String dest) {
            adjList.computeIfAbsent(src, k -> new LinkedList<>()).add(dest);
        }

        public LinkedList<String> bfs(String start) {
            LinkedList<String> result = new LinkedList<>();
            Queue<String> queue = new LinkedList<>();
            queue.add(start);

            while (!queue.isEmpty()) {
                String node = queue.poll();
                if (!result.contains(node)) {
                    result.add(node);
                    if (adjList.containsKey(node)) {
                        queue.addAll(adjList.get(node));
                    }
                }
            }
            return result;
        }
    }

    /**
     * TaxCalculator class to perform complex tax calculations for Indian Income Tax Slabs.
     */
    static class TaxCalculator {
        public static double calculateTax(double income, String filingStatus) {
            double tax = 0;

            // Indian Income Tax Slabs (FY 2023-24)
            if (income <= 400000) {
                tax = 0;
            } else if (income <= 800000) {
                tax = (income - 400000) * 0.05;
            } else if (income <= 1200000) {
                tax = 20000 + (income - 800000) * 0.10;
            } else if (income <= 1600000) {
                tax = 60000 + (income - 1200000) * 0.15;
            } else if (income <= 2000000) {
                tax = 120000 + (income - 1600000) * 0.20;
            } else if (income <= 2400000) {
                tax = 200000 + (income - 2000000) * 0.25;
            } else {
                tax = 300000 + (income - 2400000) * 0.30;
            }
            return tax;
        }
    }

    /**
     * DeductionIdentifier class to identify eligible deductions.
     */
    static class DeductionIdentifier {
        public static String identifyDeductions(HashMap<String, String> userInfo) {
            StringBuilder deductions = new StringBuilder();
            double income = Double.parseDouble(userInfo.get("income"));
            boolean hasHomeLoan = Boolean.parseBoolean(userInfo.getOrDefault("hasHomeLoan", "false"));
            boolean hasEducationLoan = Boolean.parseBoolean(userInfo.getOrDefault("hasEducationLoan", "false"));

            if (income < 500000) {
                deductions.append("Standard Deduction, ");
            }
            if (hasHomeLoan) {
                deductions.append("Home Loan Interest Deduction, ");
            }
            if (hasEducationLoan) {
                deductions.append("Education Loan Interest Deduction, ");
            }
            return deductions.toString();
        }
    }

    /**
     * eFileTaxForms: Simulates e-filing tax forms.
     */
    private static String eFileTaxForms(HashMap<String, String> userInfo) {
        // Simulate e-filing process
        return "Your tax forms have been successfully e-filed!";
    }

}
