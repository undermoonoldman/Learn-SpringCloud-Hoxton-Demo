package com.undermoonoldman.springcloud.service.impl;

import com.undermoonoldman.springcloud.entity.Payment;
import com.undermoonoldman.springcloud.dao.PaymentDao;
import com.undermoonoldman.springcloud.service.PaymentService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * (Payment)表服务实现类
 */
@Service
public class PaymentServiceImpl implements PaymentService {
    @Resource
    private PaymentDao paymentDao;

    @Override
    public Payment getPaymentById(Long id) {
        return paymentDao.getPaymentById(id);
    }

    @Override
    public int create(Payment payment) {
        return paymentDao.create(payment);
    }
}