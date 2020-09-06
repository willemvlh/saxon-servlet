This program exposes a REST API to perform XSLT transformations using the Saxon XSLT processor.

Start the server by running `java -jar saxon-1.x.jar`.

Transformations can then be invoked by sending an HTTP POST call to the server.
This call must contain a `multipart/form-data` body with two items:

* xml : a file containing the input XML.
* xsl: a file containing the stylesheet.

The response will contain the serialized result in its body.

In case of an error, the response will contain a JSON object describing the error.
Following command-line options are available:

* `-c`, `--config`: Location to Saxon configuration XML file
* `-h,--help`: Display help
* `-i, --insecure`: Run with default (insecure) configuration.*
* `-p, --port <arg>`: Port on which the server runs
* `-v, --version`: Display Saxon version info


\* This enables external function calls, retrieval of system properties and environment variables and connecting to arbitrary URLs.
This option cannot be set in combination with `--config`.

You can build from source by running the Maven command `mvn install`.