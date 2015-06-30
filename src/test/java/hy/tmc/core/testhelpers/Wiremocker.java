
package hy.tmc.core.testhelpers;

import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;


public class Wiremocker {
    
    public WireMockServer wiremockSubmitPaths() {
        WireMockServer wireMockServer = new WireMockServer();
        wireMockServer.start();

        wireMockServer.stubFor(get(urlEqualTo("/user"))
                .withHeader("Authorization", containing("Basic dGVzdDoxMjM0"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                )
        );
        wiremockGET(wireMockServer, "/courses.json?api_version=7", ExampleJson.allCoursesExample);
        return wireMockServer;
    }

    public void wiremockFailingSubmit(WireMockServer server) {
        wiremockGET(server, "/courses/313.json?api_version=7", ExampleJson.failingCourse);
        wiremockPOST(server, "/exercises/285/submissions.json?api_version=7", ExampleJson.failedSubmitResponse);
        wiremockGET(server, "/submissions/7777.json?api_version=7", ExampleJson.failedSubmission);
    }

    public void wireMockSuccesfulSubmit(WireMockServer server) {
        wiremockGET(server, "/courses/3.json?api_version=7", ExampleJson.courseExample);
        wiremockPOST(server, "/exercises/286/submissions.json?api_version=7", ExampleJson.pasteResponse);
        wiremockGET(server, "/submissions/1781.json?api_version=7", ExampleJson.successfulSubmission);
    }

    public void wireMockExpiredSubmit(WireMockServer server) {
        wiremockGET(server, "/courses/21.json?api_version=7", ExampleJson.expiredCourseExample);
    }

    /*
     * When httpGet-request is sent to http://127.0.0.1:8080/ + urlToMock, wiremock returns returnBody
     */
    private void wiremockGET(WireMockServer server, final String urlToMock, final String returnBody) {
        server.stubFor(get(urlEqualTo(urlToMock))
                .willReturn(aResponse()
                        .withBody(returnBody)
                )
        );
    }
    
    /*
    * When httpPost-request is sent to http://127.0.0.1:8080/ + urlToMock, wiremock returns returnBody
    */
    private void wiremockPOST(WireMockServer server, final String urlToMock, final String returnBody) {
        server.stubFor(post(urlEqualTo(urlToMock))
                .willReturn(aResponse()
                        .withBody(returnBody)
                )
        );
    }
}
