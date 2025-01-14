package com.padellevel.data;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "service_principal")
public class ServicePrincipal {
    @Id
    private String appId;
    private String secret;
    private String tenantId;
    private LocalDateTime expirationDate;
    private String comments;
    private String uso;

    @Enumerated(EnumType.STRING)
    private Entorno entorno;

    public enum Entorno {
        DEV, UAT, PRO
    }

    public ServicePrincipal() {
    }

    public ServicePrincipal(String appId, String secret, String tenantId, LocalDateTime expirationDate, String comments, Entorno entorno, String uso) {
        this.appId = appId;
        this.secret = secret;
        this.tenantId = tenantId;
        this.expirationDate = expirationDate;
        this.comments = comments;
        this.entorno = entorno;
        this.uso = uso;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getUso() {
        return uso;
    }

    public void setUso(String uso) {
        this.uso = uso;
    }

    public Entorno getEntorno() {
        return entorno;
    }

    public void setEntorno(Entorno entorno) {
        this.entorno = entorno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServicePrincipal that = (ServicePrincipal) o;

        return appId != null ? appId.equals(that.appId) : that.appId == null;
    }

    @Override
    public int hashCode() {
        return appId != null ? appId.hashCode() : 0;
    }
}