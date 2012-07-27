import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.cdo.CDOObject;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.net4j.CDONet4jUtil;
import org.eclipse.emf.cdo.net4j.CDOSessionConfiguration;
import org.eclipse.emf.cdo.server.CDOServerUtil;
import org.eclipse.emf.cdo.server.IRepository;
import org.eclipse.emf.cdo.server.IStore;
import org.eclipse.emf.cdo.server.mem.MEMStoreUtil;
import org.eclipse.emf.cdo.server.net4j.CDONet4jServerUtil;
import org.eclipse.emf.cdo.session.CDOSession;
import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.eclipse.emf.cdo.view.CDOView;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.examples.extlibrary.EXTLibraryFactory;
import org.eclipse.emf.examples.extlibrary.EXTLibraryPackage;
import org.eclipse.emf.examples.extlibrary.Library;
import org.eclipse.emf.examples.extlibrary.Writer;
import org.eclipse.emf.internal.cdo.CDOObjectImpl;
import org.eclipse.emf.validation.model.EvaluationMode;
import org.eclipse.emf.validation.service.IBatchValidator;
import org.eclipse.emf.validation.service.ModelValidationService;
import org.eclipse.net4j.Net4jUtil;
import org.eclipse.net4j.acceptor.IAcceptor;
import org.eclipse.net4j.connector.IConnector;
import org.eclipse.net4j.jvm.JVMUtil;
import org.eclipse.net4j.tcp.TCPUtil;
import org.eclipse.net4j.util.container.IManagedContainer;
import org.eclipse.net4j.util.container.IPluginContainer;
import org.eclipse.net4j.util.om.OMPlatform;
import org.eclipse.net4j.util.om.log.PrintLogHandler;
import org.eclipselabs.cdolight.utils.CDOTracingUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;



public class CDOPerformanceTests {
	
	  public enum ConType {
		    TCP,
		    JVM;

		    @Override
		    public String toString() {
		      return name().toLowerCase();
		    }
		  }
	  
	
	private static final String REPO_NAME = "TESTI";

	public static final long MAX_BYTES_IN_SESSION = 209715200;  // 200 MB
//  public static final long MAX_BYTES_IN_SESSION = 20971520;  // 20 MB
//  public static final long MAX_BYTES_IN_SESSION = 5242880;	// 5 MB
//  public static final long MAX_BYTES_IN_SESSION = 1242880;	// 1 MB

	private static int size   = 400;
	private StoreType storeType = StoreType.MEM;

	private IStore store;
	
	
	public enum StoreType{
		H2,RAF,MEM
	}
	
	private static EObject model;
	  private static final long SESSION_TIMEOUT = 3000 * 1000L;
	  private static final int COMMIT_TIMEOUT = (int) (SESSION_TIMEOUT / 1000L);
	private static CDOSession session;

	@Before
	public void setUp() throws Exception {
		EPackage.Registry.INSTANCE.put(EXTLibraryPackage.eNS_URI, EXTLibraryPackage.eINSTANCE);
		System.out.println("CDOPerformanceTests.setUp()");
	}

	@Test
	public void testStartCDOServer() throws IOException {
		startCDOServer();
		
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("StartCDOServer.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testStartCDOSession() throws IOException {
		IStoreConfig storeConfig = startCDOServer();
		openCDOSession(storeConfig);
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("StartCDOSession.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testOpenCDOTransaction() throws IOException {
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		sess.openTransaction();
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("OpenCDOTransaction.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testCreateCDOResource() throws IOException {	
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		CDOTransaction tx = sess.openTransaction();
		createCDOResource(tx);
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("CreateCDOResource.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testAddModelToCDOResource() throws IOException {	
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		CDOTransaction tx = sess.openTransaction();
		CDOResource res = createCDOResource(tx);
		addModelToCDOResource(EXTLibraryFactory.eINSTANCE.createLibrary(), res);
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("AddModelToCDOResource.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testCDOTransactionCommit() throws CommitException, IOException {	
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		CDOTransaction tx = sess.openTransaction();
		CDOResource res = createCDOResource(tx);
		addModelToCDOResource(EXTLibraryFactory.eINSTANCE.createLibrary(), res);
		tx.commit();
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("CDOTransactionCommit.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testCDOTransactionClose() throws CommitException, IOException {	
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		CDOTransaction tx = sess.openTransaction();
		CDOResource res = createCDOResource(tx);
		addModelToCDOResource(EXTLibraryFactory.eINSTANCE.createLibrary(), res);
		tx.commit();
		tx.close();
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("CDOTransactionClose.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testCDOSessionClose() throws CommitException, IOException {	
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		CDOTransaction tx = sess.openTransaction();
		CDOResource res = createCDOResource(tx);
		addModelToCDOResource(EXTLibraryFactory.eINSTANCE.createLibrary(), res);
		tx.commit();
		tx.close();
		sess.close();
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("CDOSessionClose.html"));
		writer.append(trace);
		writer.close();
	}
	
	@Test
	public void testOpenCDOView() throws IOException {
		IStoreConfig storeConfig = startCDOServer();
		CDOSession sess = openCDOSession(storeConfig);
		sess.openView();
		String trace = CDOTracingUtils.dumpHtmlTrace();
		BufferedWriter writer = new BufferedWriter(new FileWriter("OpenCDOView.html"));
		writer.append(trace);
		writer.close();
	}


	@Test @Ignore
	public void testCreateModel() {
		ResourceSet rSet = new ResourceSetImpl();
		rSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = rSet.createResource(URI.createURI("testdump.xmi"));
		System.out.println("CDOPerformanceTests.testCreateModel()");
		long begin = System.currentTimeMillis();
		Library lib = EXTLibraryFactory.eINSTANCE.createLibrary();
		resource.getContents().add(lib);
		lib.setName("TestLib");
		int lastPerc = 0;
		for(int i=0;i<=size    ;i++){
			int perc = (int) ((i/(double)size)*100);
			if(perc>lastPerc){
				lastPerc= perc;
				System.out.println(perc +"%");
			}
			Library l  = EXTLibraryFactory.eINSTANCE.createLibrary();
			lib.getBranches().add(l);
		}
		System.out.println("CDOPerformanceTests.testCreateModel() - took: " + (System.currentTimeMillis()-begin));
		try {
			System.out.println("CDOPerformanceTests.testCreateModel() - dump");
			resource.save(null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		model = lib;
		if(model instanceof CDOObject){
			commit(model);
		}
	}

	private void commit(EObject model2) {
		try{
			IStoreConfig storeConfig = startCDOServer();		    
		    session = openCDOSession(storeConfig);
		    CDOTransaction tx = session.openTransaction();
		    CDOResource res = createCDOResource(tx);
		    System.out.println("CDOPerformanceTests.commit() - add contents to CDOResource");
		    addModelToCDOResource(model2, res);
		    tx.commit();
		    tx.close();
		    System.out.println("CDOPerformanceTests.commit() - finished");
		}catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void addModelToCDOResource(EObject model2, CDOResource res) {
		res.getContents().add(model2);
	}

	private CDOResource createCDOResource(CDOTransaction tx) {
		return tx.createResource("libraryRes");
	}

	private IStoreConfig startCDOServer() {
		IStoreConfig storeConfig = null ;
		switch(storeType){
			case MEM: storeConfig = new MemStoreConfig(REPO_NAME);
				break;
			
			default: ;
		}
		startCDOServer(storeConfig, ConType.JVM, "127.0.0.1","2036");
		return storeConfig;
	}
	
	
	private CDOSession openCDOSession(IStoreConfig storeConfig) {
		return openCDOSession(storeConfig, "2036");
	}

	
	  private static synchronized CDOSession openCDOSession(IStoreConfig storeConfig, String hostPort) {
		    System.out.println("PLBlueArXProjectSession.openSession()");

		    IConnector connector = createConnector(ConType.JVM, hostPort);

		    CDOSessionConfiguration config = CDONet4jUtil.createSessionConfiguration();
		    config.setConnector(connector);
		    config.setRepositoryName(storeConfig.getRepositoryName());
		    org.eclipse.emf.cdo.net4j.CDOSession cdoSession = config.openSession();
		    cdoSession.getPackageRegistry().putEPackage(EPackage.Registry.INSTANCE.getEPackage(EXTLibraryPackage.eNS_URI));
		    ((org.eclipse.emf.cdo.net4j.CDOSession) cdoSession).options().getProtocol().setTimeout(SESSION_TIMEOUT);
		    ((org.eclipse.emf.cdo.net4j.CDOSession) cdoSession).options().setCommitTimeout(COMMIT_TIMEOUT);
		    return cdoSession;
		  }
	


	@Test @Ignore
	public void testValidateModel(){
		try{
			InputStreamReader ired = new InputStreamReader(System.in);
			System.out.println("press any key...");
			while(!ired.ready()){}
			while(ired.ready()){ired.read();}
			System.out.println("CDOPerformanceTests.testValidateModel()");
			long begin = System.currentTimeMillis();
			ModelValidationService.getInstance().loadXmlConstraintDeclarations();
			IBatchValidator validator = ModelValidationService.getInstance().newValidator(EvaluationMode.BATCH);
			validator.setOption(IBatchValidator.OPTION_INCLUDE_LIVE_CONSTRAINTS, true);
			validator.setOption(IBatchValidator.OPTION_TRACK_RESOURCES, true);
			final IStatus status;
			if(model instanceof CDOObjectImpl){
				CDOView view = session.openView();
				CDOObject lib = view.getObject(CDOIDUtil.createLong(2));
				lib.eContents();
				status = validator.validate(lib);
			}else{
				status = validator.validate(model);
			}
			System.out.println(status);
			System.out.println("CDOPerformanceTests.testValidateModel() - took : " + (System.currentTimeMillis() -begin));
			System.out.println("press any key...");
			while(!ired.ready()){}
		}catch (Throwable e) {
			e.printStackTrace();
		}
	}
	


	  private static IConnector createConnector(ConType conType, String host) {
	    if (conType == ConType.TCP) {
	      return Net4jUtil.getConnector(IPluginContainer.INSTANCE, conType.toString(), host);
	    }
	    else if (conType == ConType.JVM) {
	      return JVMUtil.getConnector(IPluginContainer.INSTANCE, "default");
	    }
	    return null;
	  }



	  private void startCDOServer(IStoreConfig storeConfig, ConType conType, String host, String hostPort) {
		    OMPlatform.INSTANCE.setDebugging(false);
		    OMPlatform.INSTANCE.addLogHandler(PrintLogHandler.CONSOLE);

		    configureSingleVMContainer(IPluginContainer.INSTANCE);

		    this.store = createStore(storeConfig);
		    Map<String, String> properties = createProperties(storeConfig.getRepositoryName());

		    IRepository repository = CDOServerUtil.createRepository(storeConfig.getRepositoryName(), this.store, properties);
		    CDOServerUtil.addRepository(IPluginContainer.INSTANCE, repository);

		    createAcceptor(conType, host,hostPort);
		  }
	  
	  private static void configureSingleVMContainer(final IManagedContainer container) {
		    // Prepare the TCP support

		    configureClientContainer(container);
		    configureServerContainer(ConType.JVM, container);
		  }
	  
	  private static void configureClientContainer(final IManagedContainer container) {
		    CDONet4jUtil.prepareContainer(container);
	  }
	  
	  private static void configureServerContainer(ConType conType,IManagedContainer container) {
		    Net4jUtil.prepareContainer(container); // Prepare the Net4j kernel
		    CDONet4jServerUtil.prepareContainer(container); // Prepare the CDO server
		    if (conType == ConType.TCP) {
		      TCPUtil.prepareContainer(container); // Prepare the TCP support
		    }
		    else if (conType == ConType.JVM) {
		      JVMUtil.prepareContainer(container);
		    }
		    
	 }
	  
	  private static IStore createStore(IStoreConfig storeConfig) {
		    IStore store = null; 
		    if(storeConfig instanceof MemStoreConfig){
		    	store = MEMStoreUtil.createMEMStore();
		    }
		    return store;
		  }
	  
	  private static IAcceptor createAcceptor(ConType conType, String host, String hostPort) {
		    if (conType == ConType.TCP) {
		      return Net4jUtil.getAcceptor(IPluginContainer.INSTANCE, conType.toString(), host + ":" + hostPort);
		    }
		    else if (conType == ConType.JVM) {
		      return JVMUtil.getAcceptor(IPluginContainer.INSTANCE, "default");
		    }
		    return null;

		  }
	  
	  private static Map<String, String> createProperties(final String REPO) {
		    Map<String, String> props = new HashMap<String, String>();
		    props.put(IRepository.Props.OVERRIDE_UUID, REPO);
		    props.put(IRepository.Props.SUPPORTING_AUDITS, Boolean.toString(false));
		    props.put(IRepository.Props.SUPPORTING_BRANCHES, Boolean.toString(false));
		    return props;
		  }
	  
}
