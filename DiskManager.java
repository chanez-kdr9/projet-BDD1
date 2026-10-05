import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DiskManager {

    private String dmDir;
    private int pageSize;
    private int pageCount;
    private List<Integer> freePages;

    public DiskManager() {
        freePages = new ArrayList<>();
    }

    public void Init(String dmDir, int pageSize) {

        this.dmDir = dmDir;
        this.pageSize = pageSize;

        this.pageCount = 0;
        this.freePages = new ArrayList<>();

        File configFile = new File(dmDir, "dm.config");

        if (configFile.exists()) {

            Properties properties = new Properties();

            try (FileInputStream input = new FileInputStream(configFile)) {

                properties.load(input);

                int oldPageSize = Integer.parseInt(
                        properties.getProperty("pageSize")
                );

                if (oldPageSize != pageSize) {
                    throw new IllegalArgumentException(
                            "La taille de page est incompatible avec les données existantes."
                    );
                }

                this.pageCount = Integer.parseInt(
                        properties.getProperty("pageCount", "0")
                );

                String savedFreePages = properties.getProperty(
                        "freePages", ""
                );

                if (!savedFreePages.isEmpty()) {

                    String[] pages = savedFreePages.split(",");

                    for (String page : pages) {
                        this.freePages.add(
                                Integer.parseInt(page)
                        );
                    }
                }

            } catch (IOException e) {

                System.out.println(
                        "Erreur lors du chargement de la configuration."
                );
            }
        }
    }

    public void Save() {

        File configFile = new File(dmDir, "dm.config");

        Properties properties = new Properties();

        properties.setProperty(
                "pageSize",
                String.valueOf(pageSize)
        );

        properties.setProperty(
                "pageCount",
                String.valueOf(pageCount)
        );

        StringBuilder freePagesString = new StringBuilder();

        for (int i = 0; i < freePages.size(); i++) {

            if (i > 0) {
                freePagesString.append(",");
            }

            freePagesString.append(
                    freePages.get(i)
            );
        }

        properties.setProperty(
                "freePages",
                freePagesString.toString()
        );

        try (FileOutputStream output = new FileOutputStream(configFile)) {

            properties.store(
                    output,
                    "DiskManager configuration"
            );

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors de la sauvegarde de la configuration."
            );
        }
    }
}