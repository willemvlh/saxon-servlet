package tv.mediagenix.transformer.app;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import tv.mediagenix.transformer.saxon.SerializationProps;
import tv.mediagenix.transformer.saxon.TransformationException;
import tv.mediagenix.transformer.saxon.actors.SaxonActor;
import tv.mediagenix.transformer.saxon.actors.SaxonActorBuilder;
import tv.mediagenix.transformer.saxon.actors.SaxonTransformerBuilder;
import tv.mediagenix.transformer.ParameterParser;
import tv.mediagenix.transformer.saxon.actors.SaxonXQueryPerformerBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Part;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

@RestController
public class TransformController {

    @PostMapping(path={"/query", "/transform"})
    public ResponseEntity<byte[]> doQuery(
            @RequestPart(name = "xml", required = false) Part xml, //use Part instead of MultipartFile so that we can also send strings
            @RequestPart(name = "xsl", required = false) Part xsl,
            @RequestParam(name = "output", required = false) String output,
            @RequestParam(name = "parameters", required = false) String parameters,
            HttpServletRequest request)
            throws Exception {
        SaxonActorBuilder builder = getBuilder(request.getRequestURI());
        Map<String, String> params = new ParameterParser().parseString(parameters);
        Map<String, String> serParams = new ParameterParser().parseString(output);
        SaxonActor tf = builder
                .setParameters(params)
                .setSerializationProperties(serParams)
                .build();
        Optional<InputStream> xmlStr = Optional.ofNullable(xml).flatMap(this::getInputStream);
        InputStream xslStr = Optional.ofNullable(xsl).flatMap(this::getInputStream)
                .orElseThrow(() -> new InvalidRequestException("No XSL supplied"));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SerializationProps props = xmlStr.isPresent()
                ? tf.act(new BufferedInputStream(xmlStr.get()), xslStr, os)
                : tf.act(xslStr, os);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(props.getContentType()));
        return new ResponseEntity<>(os.toByteArray(), headers, HttpStatus.OK);
    }

    private Optional<InputStream> getInputStream(Part p){
        try {
            String contentType = p.getContentType();
            if ("application/gzip".equalsIgnoreCase(contentType)) {
                return Optional.of(getZippedStreamFromPart(p.getInputStream()));
            }
            return Optional.ofNullable(p.getInputStream());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private InputStream getZippedStreamFromPart(InputStream input) {
        try {
            GZIPInputStream s = new GZIPInputStream(input);
            ZippedStreamReader r = new ZippedStreamReader();
            return r.unzipStream(s);
        } catch (IOException e) {
            throw new InvalidRequestException(e);
        }
    }

    private SaxonActorBuilder getBuilder(String requestURI) {
        switch(requestURI) {
            case "/query": return new SaxonXQueryPerformerBuilder();
            case "/transform": return new SaxonTransformerBuilder();
            default: throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}