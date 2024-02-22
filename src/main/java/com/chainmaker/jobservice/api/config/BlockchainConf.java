package com.chainmaker.jobservice.api.config;

import com.chainmaker.jobservice.api.service.BlockchainContractService;
import com.chainmaker.jobservice.api.service.JobParserService;
import org.chainmaker.sdk.ChainManager;
import org.chainmaker.sdk.Node;
import org.chainmaker.sdk.config.NodeConfig;
import org.chainmaker.sdk.config.SdkConfig;
import org.chainmaker.sdk.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class BlockchainConf {
    @Autowired
    BlockchainContractService blockchainContractService;
    @Autowired
    JobParserService jobParserService;
    @PostConstruct
    public void init() {
        try {
            Yaml yaml = new Yaml();
            SdkConfig sdkConfig;

//            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/home/wangsen/sdk_config.yml");
//            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sdk_config.yml");
//            sdkConfig = yaml.loadAs(in, SdkConfig.class);

            String sdkCP = System.getenv("SDKCP");
            if (sdkCP == null || sdkCP.isEmpty()) {
                sdkCP = "/home/workspace/sdk/sdk_config.yml";
                boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
                if(isWindows){
//                    sdkCP = this.getClass().getClassLoader().getResource("sdk_config.yml").getPath();
                    sdkCP = Paths.get(System.getProperty("user.dir")).resolve("sdk_config_test.yml").toAbsolutePath().toString();
                }
//                sdkCP = "D://sdk_config_test.yml";
            }
            ;
            FileReader reader = new FileReader(sdkCP);
//            FileReader reader=new FileReader("sdk_config.yml");
            BufferedReader buffer = new BufferedReader(reader);
            sdkConfig = yaml.loadAs(buffer, SdkConfig.class);
            reader.close();
            buffer.close();


//            in.close();
            List<Node> nodeList = new ArrayList<>();

            for (NodeConfig nodeConfig : sdkConfig.getChain_client().getNodes()) {
                List<byte[]> tlsCaCertList = new ArrayList<>();
                for (String rootPath : nodeConfig.getTrustRootPaths()) {
                    List<String> filePathList = FileUtils.getFilesByPath(rootPath);
                    for (String filePath : filePathList) {
                        tlsCaCertList.add(FileUtils.getFileBytes(filePath));
                    }
                }
                byte[][] tlsCaCerts = new byte[tlsCaCertList.size()][];
                tlsCaCertList.toArray(tlsCaCerts);
                nodeConfig.setTrustRootBytes(tlsCaCerts);
            }
            ChainManager chainManager = ChainManager.getInstance();

            blockchainContractService.setChainClient(chainManager.getChainClient(sdkConfig.getChain_client().getChainId()));

            if (blockchainContractService.getChainClient() == null) {
                blockchainContractService.setChainClient(chainManager.createChainClient(sdkConfig));
            }
            blockchainContractService.setAdminUserList(AdminLoader.loadAdminList(sdkConfig));
        } catch (Exception e) {
            System.err.println(e.fillInStackTrace());
        }

    }
}