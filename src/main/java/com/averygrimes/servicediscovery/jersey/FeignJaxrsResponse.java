package com.averygrimes.servicediscovery.jersey;


import com.averygrimes.servicediscovery.interaction.ConsulDiscoveryClient;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Util;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.util.CharsetUtils;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.internal.guava.MoreObjects;
import org.glassfish.jersey.message.internal.HeaderUtils;
import org.glassfish.jersey.message.internal.HeaderValueException;
import org.glassfish.jersey.message.internal.HttpHeaderReader;
import org.glassfish.jersey.message.internal.LanguageTag;
import org.glassfish.jersey.message.internal.Statuses;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Avery Grimes-Farrow
 * Created on: 9/30/19
 * https://github.com/helloavery
 */

public class FeignJaxrsResponse extends Response {

    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
    private ObjectMapper objectMapper;
    private Gson gsonMapper;
    private ByteArrayOutputStream entity;
    private StatusType statusType;
    private boolean hasEntity = false;

    public FeignJaxrsResponse(final feign.Response feignResponse){
        this(feignResponse, null);
    }

    public <T> FeignJaxrsResponse(final feign.Response feignResponse, Class<T> serviceInterface) {
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        gsonMapper = initGson();
        statusType = Statuses.from(feignResponse.status());
        initHeaders(feignResponse);
        entity = serviceInterface == ConsulDiscoveryClient.class ? getStream(trimWhitespaceFromInputStream(feignResponse)) : getStream(feignResponse);
    }

    private Gson initGson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setLenient();
        return gsonBuilder.create();
    }

    private void initHeaders(feign.Response feignResponse){
        for(Map.Entry<String, Collection<String>> feignHeader : feignResponse.headers().entrySet()){
            feignHeader.getValue().forEach(headerValue -> headers.add(feignHeader.getKey(), headerValue));
        }
    }

    private InputStream trimWhitespaceFromInputStream(feign.Response feignResponse){
        try{
            InputStream inputStream = feignResponse.body().asInputStream();
            BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.replace("\r", "")
                        .replace("\n", ""));
            }
            return IOUtils.toInputStream(sb.toString().replaceAll("\\s+", ""), StandardCharsets.UTF_8);
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private ByteArrayOutputStream getStream(feign.Response feignResponse){
        try{
            return getStream(feignResponse.body().asInputStream());
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private ByteArrayOutputStream getStream(InputStream inputStream){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        try{
            while ((read = inputStream.read(buffer)) != -1) {
                hasEntity = true;
                byteArrayOutputStream.write(buffer, 0, read);
            }
        }
        catch(IOException e){
            throw new ProcessingException(e);
        }
        finally{
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new ProcessingException(e);
            }
        }
        return byteArrayOutputStream;
    }


    public <T> T decode(Class<T> type) throws IOException{
        if(getStatus() == 404) return (T) Util.emptyValueOf(type);
        if(!hasEntity || entity == null || getStatus() == 204) return null;
        if(String.class.equals(type)){
            return (T) entity.toString(CharsetUtils.get(getHeaderString(HttpHeaders.CONTENT_TYPE)).name());
        }
        return objectMapper.readValue(entity.toByteArray(), objectMapper.constructType(type));
    }

    public <T> T decode(GenericType<T> type) throws IOException{
        if(getStatus() == 404) return (T) Util.emptyValueOf(type.getType());
        if(!hasEntity || entity == null || getStatus() == 204) return null;
        Reader targetReader = new InputStreamReader(new ByteArrayInputStream(this.entity.toByteArray()));
        return gsonMapper.fromJson(targetReader, type.getType());
        //return objectMapper.readValue(this.entity.toByteArray(), objectMapper.constructType(type.getType()));
        //return gsonMapper.fromJson(this.entity.toString(), type.getType());
    }

    private <T> T singleHeader(String name, Function<String, T> converter, boolean convertNull){
        final List<String> values = this.headers.get(name);
        if(CollectionUtils.isEmpty(values)){
            return convertNull ? converter.apply(null) : null;
        }
        if(values.size() > 1){
            throw new HeaderValueException(LocalizationMessages.TOO_MANY_HEADER_VALUES(name, values.toString()),
                    HeaderValueException.Context.INBOUND);
        }
        Object value = values.get(0);
        if(value == null){
            return convertNull ? converter.apply(null) : null;
        }
        try{
            return converter.apply(HeaderUtils.asString(value, null));
        }
        catch(ProcessingException e){
            throw exception(name, value, e);
        }
    }

    private static HeaderValueException exception(final String headerName, Object headerValue, Exception e){
        return new HeaderValueException(LocalizationMessages.UNABLE_TO_PARSE_HEADER_VALUE(headerName, headerValue), e,
        HeaderValueException.Context.INBOUND);
    }

    @Override
    public int getStatus() {
        return statusType.getStatusCode();
    }

    @Override
    public StatusType getStatusInfo() {
        return statusType;
    }

    @Override
    public ByteArrayOutputStream getEntity() {
        return entity;
    }

    @Override
    public <T> T readEntity(Class<T> aClass) {
        try{
            return decode(aClass);
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T readEntity(GenericType<T> genericType) {
        try{
            return decode(genericType);
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T readEntity(Class<T> aClass, Annotation[] annotations) {
        try{
            return decode(aClass);
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T readEntity(GenericType<T> genericType, Annotation[] annotations) {
        try{
            return decode(genericType);
        }
        catch(IOException e){
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasEntity() {
        return hasEntity;
    }

    @Override
    public boolean bufferEntity() {
        return false;
    }

    @Override
    public void close() {
        try{
            entity.close();
        }
        catch(IOException e){
            throw new ProcessingException(e.getMessage(), e);
        }
        finally{
            entity = null;
        }
    }

    @Override
    public MediaType getMediaType() {
        return singleHeader(HttpHeaders.CONTENT_TYPE, input -> {
            try{
                return MediaType.valueOf(input);
            }
            catch(IllegalArgumentException e){
                throw new ProcessingException(e);
            }
        }, false);
    }

    @Override
    public Locale getLanguage() {
        return singleHeader(HttpHeaders.CONTENT_LANGUAGE, input -> {
            try{
                return new LanguageTag(input).getAsLocale();
            }
            catch(ParseException e){
                throw new ProcessingException(e);
            }
        }, false);
    }

    @Override
    public int getLength() {
        return singleHeader(HttpHeaders.CONTENT_LENGTH, input -> {
            try{
                return (input != null && !input.isEmpty()) ? Integer.parseInt(input) : -1;
            }
            catch(NumberFormatException e){
                throw new ProcessingException(e);
            }
        }, true);
    }

    @Override
    public Set<String> getAllowedMethods() {
        final String allowed = getHeaderString(HttpHeaders.ALLOW);
        if(allowed  == null || allowed.isEmpty()){
            return Collections.emptySet();
        }
        try{
            return new HashSet<>(HttpHeaderReader.readStringList(allowed.toUpperCase()));
        }
        catch(ParseException e){
            throw exception(HttpHeaders.ALLOW, allowed, e);
        }
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        List<String> cookies = this.headers.get(HttpHeaders.SET_COOKIE);
        if(CollectionUtils.isEmpty(cookies)){
            return Collections.emptyMap();
        }
        Map<String, NewCookie> result = new HashMap<>();
        cookies.forEach(cookie -> {
            if(cookie != null){
                NewCookie newCookie = HttpHeaderReader.readNewCookie(cookie);
                result.put(newCookie.getName(), newCookie);
            }
        });
        return result;
    }

    @Override
    public EntityTag getEntityTag() {
        return singleHeader(HttpHeaders.ETAG, EntityTag::valueOf, false);
    }

    @Override
    public Date getDate() {
        return singleHeader(HttpHeaders.DATE, input -> {
            try{
                return HttpHeaderReader.readDate(input);
            }
            catch(ParseException e){
                throw new ProcessingException(e);
            }
        }, false);
    }

    @Override
    public Date getLastModified() {
        return singleHeader(HttpHeaders.LAST_MODIFIED, input -> {
            try{
                return HttpHeaderReader.readDate(input);
            }
            catch(ParseException e){
                throw new ProcessingException(e);
            }
        }, false);
    }

    @Override
    public URI getLocation() {
        return singleHeader(HttpHeaders.LOCATION, s -> {
            try{
                return URI.create(s);
            }
            catch(IllegalArgumentException e){
                throw new ProcessingException(e);
            }
        }, false);
    }

    @Override
    public Set<Link> getLinks() {
        return null;
    }

    @Override
    public boolean hasLink(String relation) {
        return false;
    }

    @Override
    public Link getLink(String relation) {
        return null;
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        return null;
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        final MultivaluedMap<String, ?> meta = this.headers;
        return (MultivaluedMap<String, Object>) meta;
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        return headers;
    }

    @Override
    public String getHeaderString(String name) {
        return headers.getFirst(name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("feignResponse", entity).toString();
    }
}
