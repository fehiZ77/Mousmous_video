package com.moustass.transactions_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "media")
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;

    @Column(name = "object_name", nullable = false, length = 500)
    private String objectName;

    @Column(name = "video_hash", nullable = false, length = 255)
    private String videoHash;

    @Column(name = "video_sig", columnDefinition = "TEXT", nullable = false)
    private String videoSig;

    @Column(name = "sign_keyId", nullable = false, length = 100)
    private String signKeyId;

    @Column(name = "sign_pk", columnDefinition = "TEXT", nullable = false)
    private String signPk;

    public Media(Transaction transaction, String objectName, String videoHash, String videoSig, String signKeyId, String signPk) {
        this.transaction = transaction;
        this.objectName = objectName;
        this.videoHash = videoHash;
        this.videoSig = videoSig;
        this.signKeyId = signKeyId;
        this.signPk = signPk;
    }

    public Media() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getVideoSig() {
        return videoSig;
    }

    public void setVideoSig(String videoSig) {
        this.videoSig = videoSig;
    }

    public String getSignKeyId() {
        return signKeyId;
    }

    public void setSignKeyId(String signKeyId) {
        this.signKeyId = signKeyId;
    }

    public String getSignPk() {
        return signPk;
    }

    public void setSignPk(String signPk) {
        this.signPk = signPk;
    }
}
