# PQL开发文档

### 功能说明

PQL是隐私计算平台的查询语言核心模块，完成用户需求到执行任务的转换与编排功能。

用户通过PQL发起计算任务后，解析模块根据语法文件对SQL语句进行解析，并与链上数据目录进行校验后生成执行计划，通过优化器完成规则优化与代价优化，生成执行任务。

### 模块划分

PQL模块主要分为以下模块：

- - 语法模块
- 解析模块
- 优化模块

- - 任务构建模块
- 合约调用模块

- - 外部接口模块

### 任务结构

#### Job

```json
{
       "jobID":       string,    # Job ID
       "jobName":     string,    # Job名称
       "jobType":     string,    # Job类型 //FQ(联邦查询)、FL(联邦学习)
       "status":      string,    # Job状态  WAITING/APPROVED/READY/RUNNING/CANCELED/FAILED/SUCCESS
       "submitter":   string,    # Job提交者ID
       "updateTime":  string,    # 状态变更时间戳
       "createTime":  string,    # 创建日期时间戳
       "requestData": string,    # 请求数据原文
       "tasksDAG":    string,    # 根据原文生成的task的DAG
       "parties":    []string,    # 参与方ID数组
}
```

#### Task

```json
{
  "version":   string,   #Task格式版本
  "jobID":    string,   # Job ID
  "taskName": string,   # Task名称
  "status":   string,   # Task状态 WAITING/INIT/SETUP/READY/RUNNING/CANCELED/FAILED/SUCCESS
  "updateTime":  string,    # 变更时间戳
  "createTime":  string,    # 创建日期时间戳
  "module": {           # Task模块
    "moduleName": string,  # 模块名称
    "params":{             # 模块参数
      "param1": string, # 参数1
      "param2": string, # 参数2
      ...
    },
  },
  "input": {      # 输入
    "data":[      # 数据
      {
        "dataName": string,  # 数据名称
        "taskSrc":  string,  # 数据来源taskName(如果数据不是由当前job内部产生，则为空)
        "dataID":   string,  # 数据ID(如果数据不是由当前job内部产生，此字段不为空)
        "domainID": string,  # 数据提供者ID(如果数据不是由当前job内部产生，此字段不为空)
        "role":     string,  # GUEST/HOST
        "params":{           # 数据参数
          "param1": string,    # 参数1
          "param2": string,    # 参数2

          ...
        },
      },  
      ...
    ],   
  },
  "output": {           # 输出
    "data":[          # 数据
      {
        "dataName": string, # 数据名称 
        "finalResult": string, # Y/N
        "domainID": string, # 数据提供者ID
        "dataID":   string, # 数据ID(由数据提供者选择填写，便于追溯)
      },  
      ...
    ],   
  },
  "parties":[
    {
      "partyID": string,  # 参与方ID,对应input data中的domainID
      "serverInfo":{      # 参与方服务信息，由参与方填写
        "ip": string,        # ip地址
        "port": string,      # 端口
        ...
      },
      "status":     string,        # 参与方服务任务状态 
      # INIT/SETUP/READY/RUNNING/CANCELED/FAILED/SUCCESS
      "timestamp":  string,    # 变更时间戳
    },
    ...
  ]
}
```

### 语句示例

#### 1.过滤

支持">"、"<"、"="、">="、"<="

```sql
SELECT TEST_B_1.ID FROM TEST_B_1 WHERE TEST_B_1.B1>5
```

任务结构

```json
"module": {
  "moduleName": "LOCALFILTER",
  "params": {
    "operator": ">",
    "constant": 5
  }
},
"input": {
  "data":[
    {
      "dataName": "TEST_B_1",
      "taskSrc":  "",
      "dataID":   "TEST_B_1",
      "domainID": "wx-org2.chainmaker.org",
      "role":     "",
      "params":{
        "table"："TEST_B_1",
        "field": "B1"
      },
    },
  ],   
},
```

#### 2.求交

支持"="，默认Inner Join类型

```sql
SELECT TEST_B_1.ID FROM TEST_B_1,TEST_C_1 WHERE TEST_B_1.ID=TEST_C_1.ID
```

任务结构

```json
"module": {
  "moduleName": "OTPSI",
  "params": {
    "joinType": "INNER",
    "operator": "="
  }
},
"input": {
  "data": [
    {
      "dataName": "TEST_B_1",
      "taskSrc": "",
      "dataID": "TEST_B_1",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_B_1",
        "field": "ID"
      }
    },
    {
      "dataName": "TEST_C_1",
      "taskSrc": "",
      "dataID": "TEST_C_1",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "server",
      "params": {
        "table": "TEST_C_1",
        "field": "ID"
      }
    }
  ]
},
```



```sql
SELECT /*+ TEEJOIN */ TEST_B_1.ID FROM TEST_B_1,TEST_C_1 WHERE TEST_B_1.ID=TEST_C_1.ID
```

任务结构

```json
"module": {
  "moduleName": "TEEPSI",
  "params": {
    "joinType": "INNER",
    "operator": "=",
    "teeHost": "192.168.40.230",
    "teePort": "30091",
    "domainID": ""
  }
},
"input": {
  "data": [
    {
      "dataName": "TEST_B_1",
      "taskSrc": "",
      "dataID": "TEST_B_1",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_B_1",
        "field": "ID"
      }
    },
    {
      "dataName": "TEST_C_1",
      "taskSrc": "",
      "dataID": "TEST_C_1",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "server",
      "params": {
        "table": "TEST_C_1",
        "field": "ID"
      }
    }
  ]
},
```

#### 3.计算

支持+、-、*、/

```sql
SELECT TEST_B_1.B1+TEST_C_1.C1-TEST_C_1.C2 FROM TEST_B_1,TEST_C_1 WHERE TEST_B_1.ID=TEST_C_1.ID
```

任务结构

```json
"module": {
  "moduleName": "MPCEXP",
  "params": {
    "function": "base",
    "expression": "x+x-x"
  }
},
"input": {
  "data": [
    {
      "dataName": "wx-org2.chainmaker.orgDID-1",
      "taskSrc": "0",
      "dataID": "",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "server",
      "params": {
        "table": "TEST_B_1",
        "field": "B1",
        "type": "3",
        "index": "[0]"
      }
    },
    {
      "dataName": "wx-org3.chainmaker.orgDID-1",
      "taskSrc": "0",
      "dataID": "",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_C_1",
        "field": "C1",
        "type": "3",
        "index": "[1]"
      }
    },
    {
      "dataName": "wx-org3.chainmaker.orgDID-1",
      "taskSrc": "0",
      "dataID": "",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_C_1",
        "field": "C2",
        "type": "3",
        "index": "[2]"
      }
    }
  ]
},
```

#### 4.聚合

支持SUM、COUNT、AVG、MAX、MIN

```sql
SELECT SUM(TEST_B_1.B1+TEST_C_1.C1) FROM TEST_B_1,TEST_C_1 WHERE TEST_B_1.ID=TEST_C_1.ID
```

任务结构

```json
"module": {
  "moduleName": "MPC",
  "params": {
    "function": "SUM",
    "expression": "x+x"
  }
},
"input": {
  "data": [
    {
      "dataName": "wx-org2.chainmaker.orgDID-1",
      "taskSrc": "0",
      "dataID": "",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "server",
      "params": {
        "table": "TEST_B_1",
        "field": "B1",
        "type": "3",
        "index": "[0]"
      }
    },
    {
      "dataName": "wx-org3.chainmaker.orgDID-1",
      "taskSrc": "0",
      "dataID": "",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_C_1",
        "field": "C1",
        "type": "3",
        "index": "[1]"
      }
    }
  ]
},
```

#### 5.联邦学习

支持HESB、HOSB、HELR、HEKMS、HELNR、HOLR、HEPR、HEFTL、HENN、HONN

```sql
SELECT 
  FL(
    is_train = true, 
    is_test = false, 
    FLLABEL(
      SOURCE_DATA = BREAST_HETERO_GGUEST, 
      with_label = true, label_type = int, 
      output_format = dense, namespace = experiment
    ), 
    FLLABEL(
      SOURCE_DATA = BREAST_HETERO_HHOST, 
      with_label = false, output_format = dense, 
      namespace = experiment
    ), 
    INTERSECTION(
      intersect_method = rsa
    ), 
    HELR(
      penalty = L2, tol = 0.0001, alpha = 0.01, 
      optimizer = rmsprop, batch_size =-1, 
      learning_rate = 0.15, init_param.init_method = zeros, 
      init_param.fit_intercept = true, 
      max_iter = 15, early_stop = diff, encrypt_param.key_length = 1024, 
      reveal_strategy = respectively, reveal_every_iter = true
    ), 
    EVAL(eval_type = binary)
  ) 
FROM 
  BREAST_HETERO_GGUEST, 
  BREAST_HETERO_HHOST
```

任务结构

```json
"module": {
  "moduleName": "FL",
  "params": {
    "intersection": {
      "intersect_method": "rsa"
    },
    "fl": {
      "is_test": "false",
      "is_train": "true"
    },
    "model": {
      "batch_size": "-1",
      "penalty": "L2",
      "early_stop": "diff",
      "reveal_strategy": "respectively",
      "tol": "0.0001",
      "model_name": "HELR",
      "optimizer": "rmsprop",
      "alpha": "0.01",
      "init_param": {
        "init_method": "zeros",
        "fit_intercept": "true"
      },
      "encrypt_param": {
        "key_length": "1024"
      },
      "reveal_every_iter": "true",
      "learning_rate": "0.15",
      "max_iter": "15"
    },
    "eval": {
      "eval_type": "binary"
    }
  }
},
"input": {
  "data": [
    {
      "dataName": "BREAST_HETERO_GGUEST",
      "taskSrc": "",
      "dataID": "BREAST_HETERO_GGUEST",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "guest",
      "params": {
        "output_format": "dense",
        "namespace": "experiment",
        "with_label": "true",
        "label_type": "int",
        "table": "BREAST_HETERO_GGUEST"
      }
    },
    {
      "dataName": "BREAST_HETERO_HHOST",
      "taskSrc": "",
      "dataID": "BREAST_HETERO_HHOST",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "host",
      "params": {
        "output_format": "dense",
        "namespace": "experiment",
        "with_label": "false",
        "label_type": null,
        "table": "BREAST_HETERO_HHOST"
      }
    }
  ]
},
```

#### 6.机密计算

支持用户定义的模型

```sql
SELECT TESTA(TEST_B_1.B1,TEST_C_1.C1) FROM TEST_B_1,TEST_C_1
```

任务结构

```json
"module": {
  "moduleName": "TEE",
  "params": {
    "methodName": "TESTA",
    "domainID": "wx-org3.chainmaker.orgDID",
    "teeHost": "172.16.12.230",
    "teePort": "30091"
  }
},
"input": {
  "data": [
    {
      "dataName": "TEST_B_1",
      "taskSrc": "",
      "dataID": "TEST_B_1",
      "domainID": "wx-org2.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_B_1",
        "field": "B1",
        "type": "3"
      }
    },
    {
      "dataName": "TEST_C_1",
      "taskSrc": "",
      "dataID": "TEST_C_1",
      "domainID": "wx-org3.chainmaker.orgDID",
      "role": "client",
      "params": {
        "table": "TEST_C_1",
        "field": "C1",
        "type": "3"
      }
    }
  ]
},
```

#### 7.服务类型

支持匿名查询PIR

```sql
SELECT * FROM TEST_B_1 WHERE TEST_B_1.ID=?
```

服务模板

```json
{
  "id": "",
  "version": "1.0.0",
  "orgDID": "",
  "serviceClass": "PirClient4Query",
  "serviceName": "匿名查询接收方",
  "nodePort": "",
  "manual": "false",
  "exposeEndpoints": [{
    "name": "PirClient",
    "form": [
      {
        "key": "description",
        "values": "匿名查询接收方服务",
        "labels": "服务描述",
        "types": "INPUT"
      },
      {
        "key": "tlsEnabled",
        "values": true,
        "labels": "通信加密",
        "types": "CHECKBOX",
        "desc":"启用TLS"
      },
      {
        "key": "serviceCa",
        "values": "",
        "labels": "签名证书",
        "types": "TEXTAREA"
      },
      {
        "key": "serviceCert",
        "values": "",
        "labels": "加密证书",
        "types": "TEXTAREA"
      },
      {
        "key": "serviceKey",
        "values": "",
        "labels": "加密私钥",
        "types": "TEXTAREA"
      },
      {
        "key": "protocol",
        "values": "HTTP",
        "labels": "通信协议",
        "types": "SELECT",
        "options": [
          {
            "value": "HTTP",
            "label": "HTTP"
          },
          {
            "value": "GRPC",
            "label": "GRPC"
          }
        ]
      }]
  }],
  "referEndpoints": [
    {
      "name": "PirServer",
      "referServiceID": "",
      "referEndpointName": "PirServer",
      "protocol": "HTTP",
      "serviceCa": "",
      "serviceCert": ""
    }
  ],
  "values": [
    {
      "key": "",
      "value": ""
    }
  ],
  "referValues": [
    {
      "key": "",
      "referServiceID": "",
      "referKey": ""
    }
  ]
}
```

### 代码实现

#### 1. 语法模块

PQL语法解析使用ANTLR4工具，将用户输入的SQL语句转换为抽象语法树AST，并提供遍历AST的接口。

ANTLR4可以根据语法定义文件生成解析器，解析器可以构建和遍历语法树。这里语法定义文件为后缀是.g4的文件，生成的解析器相关代码在/gen目录下。

编译器IntelliJ安装插件ANTLR4后可以直接查看SQL语句对应的语法树

例如，将SQL语句`SELECT ADATA.A1 FROM ADATA,BDATA WHERE ADATA.ID=BDATA.ID`通过语法文件解析成语法树后，如下图所示：

![img](https://cdn.nlark.com/yuque/0/2023/png/12738938/1697420726985-4107310b-2a5d-4878-9750-1f1176636b61.png)

语法定义文件分为词法部分和语法部分，在本项目中对应的是SqlBaseLexer.g4和SqlBaseParser.g4，Lexer定义了查询语言支持的所有单词符号，Parser定义了支持的所有语法逻辑。PQL项目通过修改语法定义文件，在SQL的基础上增加了FL、TEE等特殊语法的支持。并通过ANTLR4工具由g4文件生成SqlBaseParser、SqlBaseParserVistor等代码文件，提供语法树的生成与遍历接口。

#### 2. 解析模块

解析模块是完成语法树到逻辑计划的转换，主要是基于上述语法树和解析器，遍历整棵语法树并将对应语义转换为逻辑计划算子。

逻辑算子如下所示：

```sql
SELECT /*+ TEEJOIN */ SUM(TEST_B_1.B1+TEST_C_1.C1) FROM TEST_B_1 JOIN TEST_C_1 ON TEST_B_1.ID=TEST_C_1.ID WHERE TEST_B_1.B2>5
```

| 逻辑算子          | 表达                                   | 说明         |
| ----------------- | -------------------------------------- | ------------ |
| LogicalTable      | FROM ... , ...                         | 源数据       |
| LogicalFilter     | WHERE ...>...                          | 过滤         |
| LogicalJoin       | FROM ... JOIN ... ON ... WHERE ...=... | 求交         |
| LogicalProject    | SELECT ... SELECT ...+...              | 表达式       |
| LogicalAggregate  | SELECT SUM()...                        | 聚合         |
| LogicalHint       | SELECT /*+ ... */ ...                  | 指定特定算子 |
| FederatedLearning | SELECT FL()...                         | 联邦学习     |

遍历语法树中的表达语句，生成对应的逻辑算子，并按照逻辑执行顺序构成一棵逻辑计划树。

#### 3.优化模块

优化模块集成了Calcite的优化器，优化器主要输入有三部分：元数据、逻辑计划、规则与代价。

基于上面三个部分可以生成优化后的执行计划。PQL项目的解析器和优化器是两个模块，因此做了逻辑计划的转换，将独立的逻辑计划转换为Calcite结构的逻辑计划，从而实现进一步优化。

- 转换器：calcite/converter、calcite/adapter
- 元数据：calcite/optimizer/metadata
- 逻辑计划：calcite/relnode
- 规则与代价：calcite/cost

#### 4.任务构建模块

任务构建类JobBuilderWithOptimizer通过遍历优化器生成的计划树，生成对应的任务结构，同时做了一些特殊场景的逻辑处理，这部分代码需要进一步优化。

#### 5.合约调用模块

合约调用模块主要集成Chainmaker的JavaSDK，通过下列配置文件连接区块链节点调用合约：

```yaml
chain_client:
  # 链ID
  chain_id: "chain1"
  # 组织ID
  org_id: "wx-org3.chainmaker.org"
  # 客户端用户私钥路径
  user_key_file_path: "crypto-config/wx-org3.chainmaker.org/user/admin1/admin1.tls.key"
  # 客户端用户证书路径
  user_crt_file_path: "crypto-config/wx-org3.chainmaker.org/user/admin1/admin1.tls.crt"
  # 客户端用户交易签名私钥路径(若未设置，将使用user_key_file_path)
  user_sign_key_file_path: "crypto-config/wx-org3.chainmaker.org/user/admin1/admin1.sign.key"
  # 客户端用户交易签名证书路径(若未设置，将使用user_crt_file_path)
  user_sign_crt_file_path: "crypto-config/wx-org3.chainmaker.org/user/admin1/admin1.sign.crt"
  # 同步交易结果模式下，轮训获取交易结果时的最大轮训次数，删除此项或设为<=0则使用默认值 10
  retry_limit: 10
  # 同步交易结果模式下，每次轮训交易结果时的等待时间，单位：ms 删除此项或设为<=0则使用默认值 500
  retry_interval: 500

  nodes:
    - # 节点地址，格式为：IP:端口:连接数192.168.0.202:12301
      node_addr: "192.168.40.243:12301"
      # 节点连接数
      conn_cnt: 10
      # RPC连接是否启用双向TLS认证
      enable_tls: true
      # 信任证书池路径
      trust_root_paths:
        - "crypto-config/wx-org1.chainmaker.org/ca"
      # TLS hostname
      tls_host_name: "chainmaker.org"
  archive:
    # 数据归档链外存储相关配置
    type: "mysql"
    dest: "root:123456:localhost:3306"
    secret_key: xxx
  rpc_client:
    # grpc客户端最大接受容量(MB)
    max_receive_message_size: 16

  pkcs11:
    enabled: false # pkcs11 is not used by default

  # 交易结果是否订阅获取
  enable_tx_result_dispatcher: false

  ##连接池配置
  connPool:
    # 最大连接数
    maxTotal: 3000
    # 最少空闲连接
    minIdle: 0
    #最大空闲连接
    maxIdle: 3000
    #连接空闲最小保活时间，默认即为30分钟(18000000)，单位：ms
    minEvictableIdleTime: 350000
    #回收空闲线程的执行周期，单位毫秒。默认值10000ms（10s） ，-1 表示不启用线程回收资源，单位：ms
    timeBetweenEvictionRuns: 10000
    #没有空闲连接时，获取连接是否阻塞
    blockWhenExhausted: true
    #当没有空闲连接时，获取连接阻塞等待时间，单位：ms
    maxWaitMillis: 3000
```

创建任务过程主要与三个合约交互：身份合约、目录合约、任务合约，合约设计参考隐私计算合约设计文档。