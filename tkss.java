import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlModifier {

    // Исходный текст для поиска
    private static final String FIND_TEXT = "ДО ПП";
    // Имя XML-элемента, который представляет собой тест-кейс
    private static final String TEST_CASE_TAG = "testScript";

    // Требуемые замены
    private static final String REPLACE_IN_ORIGINAL = "ДО ПО";
    private static final List<String> REPLACE_IN_CLONES = List.of("ДО ПКО", "ДО ПТ", "ДО ПИ");

    public static void main(String[] args) {
        String inputFileName = "test-k.xml";
        String outputFileName = "modified_" + inputFileName;

        try {
            // 1. Загрузка документа
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(inputFileName));

            // Получаем корневой элемент <project>
            Element projectRoot = doc.getDocumentElement();

            // 2. Находим и сохраняем оригинальные тест-кейсы (3 штуки)
            NodeList originalTestScriptsList = projectRoot.getElementsByTagName(TEST_CASE_TAG);
            List<Node> originalTestCases = new ArrayList<>();
            for (int i = 0; i < originalTestScriptsList.getLength(); i++) {
                originalTestCases.add(originalTestScriptsList.item(i));
            }

            // 3. Выполнение модификаций и клонирования
            executeModifications(doc, projectRoot, originalTestCases);

            // 4. Сохранение результата
            writeXmlToFile(doc, outputFileName);

            System.out.println("✅ Обработка завершена. Создано 12 тест-кейсов.");
            System.out.println("Результат сохранен в файл: " + outputFileName);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("\n❌ Ошибка при обработке XML. Убедитесь, что файл '" + inputFileName + "' существует и является корректным XML.");
        }
    }

    /**
     * Исправленная логика по модификации и клонированию.
     */
    private static void executeModifications(Document doc, Element root, List<Node> originalTestCases) {

        // =========================================================================
        // ИСПРАВЛЕНИЕ ОШИБКИ: 
        // 1. Создаем список-шаблон (глубокие копии), где все еще содержится "ДО ПП".
        List<Node> templateTestCases = new ArrayList<>();
        for (Node originalCase : originalTestCases) {
            // Создаем копию с исходным текстом "ДО ПП"
            templateTestCases.add(originalCase.cloneNode(true));
        }
        // =========================================================================

        // --- Шаг 1: Замена в оригинальных тест-кейсах (ДО ПП -> ДО ПО) ---
        // Модифицируем узлы, которые УЖЕ находятся в документе.
        System.out.println("-> Шаг 1: Замена '" + FIND_TEXT + "' на '" + REPLACE_IN_ORIGINAL + "' в оригинальных тест-кейсах (3 шт.).");
        for (Node originalCase : originalTestCases) {
            replaceTextRecursively(originalCase, FIND_TEXT, REPLACE_IN_ORIGINAL);
        }

        // --- Шаги 2, 3, 4: Клонирование и замена (3 x 3 = 9 новых ТК) ---
        // Используем templateTestCases, где все еще "ДО ПП".
        for (String cloneReplacement : REPLACE_IN_CLONES) {
            System.out.println("-> Клонирование и замена '" + FIND_TEXT + "' на '" + cloneReplacement + "' (3 новых ТК).");

            for (Node templateCase : templateTestCases) {
                // Создаем глубокую копию из ШАБЛОНА (который содержит "ДО ПП")
                Node clonedCase = templateCase.cloneNode(true);

                // Выполняем замену в клоне (заменяем "ДО ПП" на нужный вариант)
                replaceTextRecursively(clonedCase, FIND_TEXT, cloneReplacement);

                // Добавляем клон в корневой элемент
                root.appendChild(doc.importNode(clonedCase, true));
            }
        }
    }

    /**
     * Рекурсивно обходит узлы и заменяет текст внутри Text или CDATA узлов.
     */
    private static void replaceTextRecursively(Node node, String findText, String replaceText) {
        if (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE) {
            String content = node.getNodeValue();
            if (content.contains(findText)) {
                String newContent = content.replace(findText, replaceText);
                node.setNodeValue(newContent);
            }
        }

        // Обходим дочерние узлы
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            replaceTextRecursively(children.item(i), findText, replaceText);
        }
    }

    /**
     * Сохраняет XML Document в файл без лишних пустых строк (отключает форматирование).
     */
    private static void writeXmlToFile(Document doc, String fileName) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Отключаем форматирование (indent), чтобы избежать проблемы с лишними пустыми строками
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));

        transformer.transform(source, result);
    }
}
