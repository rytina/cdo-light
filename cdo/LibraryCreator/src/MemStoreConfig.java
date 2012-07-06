


public class MemStoreConfig implements IStoreConfig{

	private final String repoName;
	
	public MemStoreConfig(String repoName){
		this.repoName = repoName;
	}

	@Override
	public String getRepositoryName() {
		return repoName;
	}
}
