


public class LissomeStoreConfig implements IStoreConfig{

	private final String repoName;
	
	public LissomeStoreConfig(String repoName){
		this.repoName = repoName;
	}

	@Override
	public String getRepositoryName() {
		return repoName;
	}
}
