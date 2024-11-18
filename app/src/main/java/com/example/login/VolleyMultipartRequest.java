package com.example.login;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Request;

import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Map;

public class VolleyMultipartRequest extends Request<NetworkResponse> {

    private final String twoHyphens = "--";
    private final String lineEnd = "\r\n";
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    private Response.Listener<NetworkResponse> mListener;
    private Response.ErrorListener mErrorListener;
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
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            // Text parameters
            Map<String, String> params = getParams();
            if (params != null && params.size() > 0) {
                textParse(bos, params, getParamsEncoding());
            }

            // Data parameters
            Map<String, DataPart> data = getByteData();
            if (data != null && data.size() > 0) {
                dataParse(bos, data);
            }

            // End boundary
            bos.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
            return bos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
        try {
            return Response.success(response,
                    HttpHeaderParser.parseCacheHeaders(response));
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
     * Override this method to provide parameters.
     */
    protected Map<String, DataPart> getByteData() throws AuthFailureError {
        return null;
    }

    private void textParse(ByteArrayOutputStream bos, Map<String, String> params, String encoding) throws IOException {
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                StringBuilder sb = new StringBuilder();
                sb.append(twoHyphens).append(boundary).append(lineEnd);
                sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"").append(lineEnd);
                sb.append(lineEnd);
                sb.append(entry.getValue()).append(lineEnd);
                bos.write(sb.toString().getBytes(encoding));
            }
        } catch (Exception e) {
            throw new IOException("Error while writing text parameters", e);
        }
    }

    private void dataParse(ByteArrayOutputStream bos, Map<String, DataPart> data) throws IOException {
        try {
            for (Map.Entry<String, DataPart> entry : data.entrySet()) {
                DataPart dataFile = entry.getValue();
                StringBuilder sb = new StringBuilder();
                sb.append(twoHyphens).append(boundary).append(lineEnd);
                sb.append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"; filename=\"").append(dataFile.getFileName()).append("\"").append(lineEnd);
                sb.append("Content-Type: ").append(dataFile.getType()).append(lineEnd);
                sb.append(lineEnd);
                bos.write(sb.toString().getBytes());

                bos.write(dataFile.getContent());
                bos.write(lineEnd.getBytes());
            }
        } catch (Exception e) {
            throw new IOException("Error while writing data parameters", e);
        }
    }

    public static class DataPart {
        private String fileName;
        private byte[] content;
        private String type;

        public DataPart() {
        }

        public DataPart(String name, byte[] data) {
            fileName = name;
            content = data;
        }

        public DataPart(String name, byte[] data, String type) {
            fileName = name;
            content = data;
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
