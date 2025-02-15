import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader{
    private Properties properties;
    private String filepath;

    public ConfigReader(String filepath){
        this.filepath = filepath;
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filepath)){
            properties.load(fis);
        } catch (Exception e) {
            System.out.println("failed to load");
        }
    }
    
    public String getProperty(String key){
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value){
        properties.setProperty(key, value);
    }

    public void saveProperty(){
        try (FileOutputStream fos = new FileOutputStream(filepath)) {
        properties.store(fos, "Updated properties file");
    } catch (IOException e) {
        System.err.println("Failed to save configuration file: ");
    }
    }
}
