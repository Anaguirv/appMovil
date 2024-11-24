package com.example.login;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private final Response.Listener<NetworkResponse> mListener;
    private final Response.ErrorListener mErrorListener;
    private Map<String, String> mHeaders;

    public VolleyMultipartRequest(int method, String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    public VolleyMultipartRequest(String url,
                                  Response.Listener<NetworkResponse> listener,
                                  Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (mHeaders != null) {
            return mHeaders;
        }
        return super.getHeaders();
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data; boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Parsear parámetros de texto
            Map<String, String> params = getParams();
            if (params != null && !params.isEmpty()) {
                textParse(bos, params);
            }

            // Parsear parámetros de archivo
            Map<String, DataPart> data = getByteData();
            if (data != null && !data.isEmpty()) {
                dataParse(bos, data);
            }

            // Agregar el cierre del límite
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response, HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new com.android.volley.ParseError(e));
        }
    }

    @Override
    protected void deliverResponse(NetworkResponse response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }

    @Override
    public void deliverError(com.android.volley.VolleyError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
    }

    /**
     * Sobrescribe este método para proporcionar parámetros.
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        return null;
    }

    /**
     * Sobrescribe este método para proporcionar datos binarios.
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    private void textParse(ByteArrayOutputStream bos, Map<String, String> params) throws IOException {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            bos.write((twoHyphens + boundary + lineEnd).getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" + key + "\"" + lineEnd).getBytes());
            bos.write((lineEnd).getBytes());
            bos.write((value + lineEnd).getBytes());
        }
    }

    private void dataParse(ByteArrayOutputStream bos, Map<String, DataPart> data) throws IOException {
        for (Map.Entry<String, DataPart> entry : data.entrySet()) {
            DataPart dataFile = entry.getValue();

            bos.write((twoHyphens + boundary + lineEnd).getBytes());
            bos.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + dataFile.getFileName() + "\"" + lineEnd).getBytes());
            bos.write(("Content-Type: " + dataFile.getType() + lineEnd).getBytes());
            bos.write((lineEnd).getBytes());

            bos.write(dataFile.getContent());
            bos.write((lineEnd).getBytes());
        }
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String fileName, byte[] content) {
            this(fileName, content, "application/octet-stream");
        }

        public DataPart(String fileName, byte[] content, String type) {
            this.fileName = fileName;
            this.content = content;
            this.type = type;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }

        public String getType() {
            return type;
        }
    }
}
