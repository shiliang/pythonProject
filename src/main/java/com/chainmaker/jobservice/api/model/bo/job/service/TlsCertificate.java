package com.chainmaker.jobservice.api.model.bo.job.service;

public class TlsCertificate {
    private String name;
    private String certificateAuthority;
    private String certificate;

    public String getCertificateAuthority() {
        return certificateAuthority;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCertificateAuthority(String certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
