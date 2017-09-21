# scala-log4j-slack
Simple log4j appender which logs directly to Slack channel


## Getting Started

### Prerequisites

* **Make sure you have Scala installed:**

````shell
$ brew install scala
````

Or visit [http://www.scala-lang.org/download/](http://www.scala-lang.org/download/) for alternative ways.

* **Install Scala Build Tool:**

```shell
$ brew install sbt
```

Or visit [http://www.scala-sbt.org/download.html](http://www.scala-sbt.org/download.html) to see more.

* **Create your custom [Slack App](https://api.slack.com/apps?new_app=1) and Choose your Workspace**

* **Add [Incoming WebHooks integration](https://api.slack.com/incoming-webhooks)**

* **Fill the gaps to `resources/classified/slack.json`**

```json
{
    "host": "hooks.slack.com",
    "endpoint": "/YourWebhookURLPath"
}
```

* **And put your logs**

```scala
val logJson: JValue = "text" -> "YOUR LOGS" 
```

### Running the tests
```shell
$ sbt project run
```

## Built With

* [Netty](http://netty.io/) - Binds the core library to a Netty channel handler and provides an embedded server.
* [json4s](http://json4s.org/) - Provides extractors for working with jsonp and transforming json request bodies.

# License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
