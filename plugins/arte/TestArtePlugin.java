import com.dabi.habitv.provider.arte.ArtePluginManager;
import com.dabi.habitv.api.plugin.dto.CategoryDTO;
import java.util.Set;

public class TestArtePlugin {
    public static void main(String[] args) {
        try {
            ArtePluginManager manager = new ArtePluginManager();
            
            System.out.println("Testing Arte Plugin...");
            
            // Test category discovery
            Set<CategoryDTO> categories = manager.findCategory();
            System.out.println("Found " + categories.size() + " categories:");
            
            for (CategoryDTO category : categories) {
                System.out.println("  - " + category.getName() + " (ID: " + category.getId() + ")");
                
                // Test replay category specifically
                if (category.getName().equals("Replay")) {
                    System.out.println("    Testing replay category...");
                    Set<EpisodeDTO> episodes = manager.findEpisode(category);
                    System.out.println("    Found " + episodes.size() + " replay episodes");
                    
                    // Show first few episodes
                    int count = 0;
                    for (EpisodeDTO episode : episodes) {
                        if (count < 5) {
                            System.out.println("      - " + episode.getName() + " -> " + episode.getId());
                            count++;
                        } else {
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error testing arte plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 