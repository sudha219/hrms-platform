package com.hrms.dto.attendance;

import com.hrms.enums.SourceChannel;
import lombok.Data;

@Data
public class CheckInRequest {
    private SourceChannel sourceChannel = SourceChannel.WEB;
}
