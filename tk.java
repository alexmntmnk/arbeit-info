import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XmlModifier {

    // Исходный текст для поиска
    private static final String FIND_TEXT = "ДО ПП";
    // Имя XML-элемента, который представляет собой тест-кейс
    private static final String TEST_CASE_TAG = "testScript";
    
    // Карта модификаций: ключ - замена в оригинале, значения - замена в клонах
    private static final Map<String, List<String>> MODIFICATIONS = new LinkedHashMap<>();
    static {
        // 1. Замена в оригинальном наборе
        // Ключ: "ДО ПО" - текст для замены в исходных тест-кейсах
        // Значение: List<String> - список текстов для замены в новых, клонированных тест-кейсах
        MODIFICATIONS.put("ДО ПО", List.of("ДО ПКО", "ДО ПТ", "ДО ПИ"));
    }

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
            
            System.out.println("✅ Обработка завершена.");
            System.out.println("Исходный файл: " + inputFileName + " (предполагаем, что он содержит 3 testScript)");
            System.out.println("Результат сохранен в файл: " + outputFileName);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("\n❌ Ошибка при обработке XML. Убедитесь, что файл '" + inputFileName + "' существует и является корректным XML.");
        }
    }

    /**
     * Основная логика по модификации и клонированию.
     */
    private static void executeModifications(Document doc, Element root, List<Node> originalTestCases) {
        
        // Получаем целевые строки замены
        String replaceInOriginal = MODIFICATIONS.keySet().iterator().next(); // ДО ПО
        List<String> replaceInClones = MODIFICATIONS.get(replaceInOriginal); // ДО ПКО, ДО ПТ, ДО ПИ
        
        // --- Шаг 1: Замена в оригинальных тест-кейсах (ДО ПП -> ДО ПО) ---
        System.out.println("-> Шаг 1: Замена '" + FIND_TEXT + "' на '" + replaceInOriginal + "' в оригинальных тест-кейсах.");
        for (Node originalCase : originalTestCases) {
            // Модифицируем узел in-place, прямо в документе
            replaceTextRecursively(originalCase, FIND_TEXT, replaceInOriginal);
        }
        
        // --- Шаги 2, 3, 4: Клонирование и замена ---
        for (String cloneReplacement : replaceInClones) {
            System.out.println("-> Шаг 2-4: Клонирование и замена '" + FIND_TEXT + "' на '" + cloneReplacement + "' (3 новых ТК).");
            
            // Для создания клонов используем исходный (немодифицированный) список.
            // Примечание: Мы клонируем узлы, которые были извлечены до их модификации в Шаге 1.
            for (Node originalCase : originalTestCases) {
                // Создаем глубокую копию оригинального узла
                Node clonedCase = originalCase.cloneNode(true); 
                
                // Выполняем замену в клоне
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
     * Сохраняет XML Document в файл с форматированием.
     */
    private static void writeXmlToFile(Document doc, String fileName) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        
        // Настройки для красивого вывода XML
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        // Устанавливаем отступ (зависит от версии JDK)
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); 
        
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(fileName));

        transformer.transform(source, result);
    }
}
