Site Parser
==============================
Test task for job application.

Very minimalistic site parser with REST API written on Scala with using SBT, Play Framework, MongoDB, Swagger.

In current version we can use the next api calls:

You can use the following command to upload a file data to the database
curl -X POST "http://localhost:9000/links" -H "accept: application/json" -H "Content-Type: multipart/form-data" -F "file=@example.tsv;type="

The following command will return a data from the database
curl -X GET "http://localhost:9000/links" -H "accept: application/json"

This command allows you to compare data from a database with links on donor sites
curl -X PUT "http://localhost:9000/links" -H "accept: application/json"

For automatic update data, we can use Cron Scheduler and run some sh script with curl command.

