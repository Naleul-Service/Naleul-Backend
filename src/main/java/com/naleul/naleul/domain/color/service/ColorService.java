package com.naleul.naleul.domain.color.service;

import com.naleul.naleul.domain.color.dto.ColorCreateRequest;
import com.naleul.naleul.domain.color.dto.ColorResponse;
import com.naleul.naleul.domain.color.entity.Color;
import com.naleul.naleul.domain.color.repository.ColorRepository;
import com.naleul.naleul.global.common.response.ErrorCode;
import com.naleul.naleul.global.exception.CustomException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService {
    private final ColorRepository colorRepository;

    @Transactional
    public ColorResponse createColor(ColorCreateRequest request) {
        Color color = Color.builder()
                .colorCode(request.getColorCode())
                .build();

        Color savedColor = colorRepository.save(color);

        return ColorResponse.from(savedColor);
    }

    @Transactional
    public ColorResponse getColorById(Long colorId) {
        Color color = colorRepository.findById(colorId)
                .orElseThrow(() -> new CustomException(ErrorCode.COLOR_NOT_FOUND));

        return ColorResponse.from(color);
    }

    @Transactional
    public List<ColorResponse> getAllColors() {
        return colorRepository.findAll()
                .stream()
                .map(ColorResponse::from)
                .toList();
    }

    public void deleteColor(Long colorId) {
        // 존재하지 않는 colorId면 예외 던지기
        if (!colorRepository.existsById(colorId)) {
            throw new CustomException(ErrorCode.COLOR_NOT_FOUND);
        }
        colorRepository.deleteById(colorId);
    }
}
