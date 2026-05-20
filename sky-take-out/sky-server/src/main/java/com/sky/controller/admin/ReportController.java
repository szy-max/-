
package com.sky.controller.admin;

import com.sky.dto.ReportDTO;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@Slf4j
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    public Result turnoverStatistics(ReportDTO reportDTO) {
        return Result.success(reportService.turnoverStatistics(reportDTO));
    }

    @GetMapping("/userStatistics")
    public Result userStatistics(ReportDTO reportDTO) {
        UserReportVO userReportVO = reportService.userStatistics(reportDTO);
        return Result.success(userReportVO);
    }

    @GetMapping("/ordersStatistics")
    public Result ordersStatistics(ReportDTO reportDTO) {
        OrderReportVO orderReportVO = reportService.ordersStatistics(reportDTO);
        return Result.success(orderReportVO);
    }

    @GetMapping("/top10")
    public Result top10(ReportDTO reportDTO) {
        SalesTop10ReportVO salesTop10ReportVO = reportService.top10(reportDTO);
        return Result.success(salesTop10ReportVO);
    }

    @GetMapping("/export")
    public Result export(HttpServletResponse response) {
        ReportDTO reportDTO = new ReportDTO(LocalDate.now().plusDays(-30), LocalDate.now());
        reportService.export(reportDTO,response);
        return Result.success();
    }
}
