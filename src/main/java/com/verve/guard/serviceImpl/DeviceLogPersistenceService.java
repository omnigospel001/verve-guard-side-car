package com.verve.guard.serviceImpl;

import com.verve.guard.entity.DeviceLog;
import com.verve.guard.repository.DeviceLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// Create a separate component for this save
@Service
public class DeviceLogPersistenceService {

    private final DeviceLogRepository deviceLogRepository;

    public DeviceLogPersistenceService(DeviceLogRepository deviceLogRepository) {
        this.deviceLogRepository = deviceLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDeviceLog(DeviceLog deviceLog) {
        deviceLogRepository.save(deviceLog);
    }
}
