package com.ly.maychu.model;

import jakarta.persistence.*;

@Entity
@Table(name = "whitelisturl")
public class WhitelistUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "idcathi")
    private CaThi caThi;

    @Column(nullable = false)
    private String url;

    private String mota;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CaThi getCaThi() { return caThi; }
    public void setCaThi(CaThi caThi) { this.caThi = caThi; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getMota() { return mota; }
    public void setMota(String mota) { this.mota = mota; }
}