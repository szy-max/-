package com.sky.service;

import com.sky.dto.ReportDTO;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;

public interface ReportService {
    TurnoverReportVO turnoverStatistics(ReportDTO reportDTO);

    UserReportVO userStatistics(ReportDTO reportDTO);

    OrderReportVO ordersStatistics(ReportDTO reportDTO);

    SalesTop10ReportVO top10(ReportDTO reportDTO);

    void export(ReportDTO reportDTO, HttpServletResponse response);
}
