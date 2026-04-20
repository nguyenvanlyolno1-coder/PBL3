package com.ly.maychu.service;

import com.ly.maychu.model.NguoiDung;
import com.ly.maychu.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Spring Security gọi hàm này khi GV đăng nhập qua form
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        NguoiDung nd = nguoiDungRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy: " + username));

        return User.builder()
                .username(nd.getUsername())
                .password(nd.getPass())
                .roles(nd.getVaiTro()) // GIANG_VIEN, SINH_VIEN, QUAN_TRI
                .build();
    }

    // Hàm này dùng cho MayTram gọi API xác thực SV
    public Optional<NguoiDung> xacThucSinhVien(String username, String rawPassword) {
        Optional<NguoiDung> opt = nguoiDungRepository.findByUsername(username);
        if (opt.isPresent()) {
            NguoiDung nd = opt.get();
            if (nd.getVaiTro().equals("SINH_VIEN")
                    && passwordEncoder.matches(rawPassword, nd.getPass())) {
                return Optional.of(nd);
            }
        }
        return Optional.empty();
    }

    // Hàm tìm user theo username (dùng ở nhiều nơi)
    public Optional<NguoiDung> findByUsername(String username) {
        return nguoiDungRepository.findByUsername(username);
    }

    // Admin đổi mật khẩu
    public void doiMatKhau(Long userId, String matKhauMoi) {
        NguoiDung nd = nguoiDungRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
        nd.setPass(passwordEncoder.encode(matKhauMoi));
        nguoiDungRepository.save(nd);
    }

    // Tạo user mới (Admin dùng)
    public NguoiDung taoNguoiDung(NguoiDung nd) {
        nd.setPass(passwordEncoder.encode(nd.getPass()));
        return nguoiDungRepository.save(nd);
    }
}