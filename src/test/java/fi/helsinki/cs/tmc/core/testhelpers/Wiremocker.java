
package fi.helsinki.cs.tmc.core.testhelpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;

public class Wiremocker {

    public WireMockServer wiremockSubmitPaths() {
        WireMockServer wireMockServer = new WireMockServer();
        wireMockServer.start();

        wireMockServer.stubFor(
                get(urlEqualTo("/user"))
                        .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                        .willReturn(aResponse().withStatus(200)));
        wiremockGet(wireMockServer, "/courses.json?api_version=7", ExampleJson.allCoursesExample);
        return wireMockServer;
    }

    public void wiremockFailingSubmit(WireMockServer server) {
        wiremockGet(server, "/courses/313.json?api_version=7", ExampleJson.failingCourse);
        wiremockPost(
                server,
                "/exercises/285/submissions.json?api_version=7",
                ExampleJson.failedSubmitResponse);
        wiremockGet(server, "/submissions/7777.json?api_version=7", ExampleJson.failedSubmission);
    }

    public void wireMockSuccesfulSubmit(WireMockServer server) {
        wiremockGet(server, "/courses/3.json?api_version=7", ExampleJson.courseExample);
        wiremockPost(
                server, "/exercises/286/submissions.json?api_version=7", ExampleJson.pasteResponse);
        wiremockGet(
                server, "/submissions/1781.json?api_version=7", ExampleJson.successfulSubmission);
    }

    public void wireMockExpiredSubmit(WireMockServer server) {
        wiremockGet(server, "/courses/21.json?api_version=7", ExampleJson.expiredCourseExample);
    }

    /*
     * When httpGet-request is sent to http://127.0.0.1:8080/ + urlToMock, wiremock returns returnBody.
     */
    private void wiremockGet(
            WireMockServer server, final String urlToMock, final String returnBody) {
        server.stubFor(get(urlEqualTo(urlToMock)).willReturn(aResponse().withBody(returnBody)));
    }

    /*
     * When httpPost-request is sent to http://127.0.0.1:8080/ + urlToMock, wiremock returns returnBody
     */
    private void wiremockPost(
            WireMockServer server, final String urlToMock, final String returnBody) {
        server.stubFor(post(urlEqualTo(urlToMock)).willReturn(aResponse().withBody(returnBody)));
    }
}
