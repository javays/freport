import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class ValidatorTest{
    
    public static boolean validate(String xml, String xsd) {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(xsd));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xml)));
            return true;
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    public static void main(String[] args) {
        String xml = Thread.currentThread().getContextClassLoader().getResource("com/resources/tmpl_xls_bill_custom_normal_layout.xml").getPath();
        String xsd = Thread.currentThread().getContextClassLoader().getResource("com/resources/freport-layout.xsd").getPath();
        System.out.println(validate(xml, xsd));
    }
}