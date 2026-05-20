package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.ReportDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO turnoverStatistics(ReportDTO reportDTO) {
        List<LocalDate> days = getDaysBetween(reportDTO);
        String dateList = "";
        String turnoverList = "";
        for (int i = 0; i < days.size(); i++) {
            LocalDate afterDay = days.get(i).plusDays(1);
            String amount = ordersMapper.sumAmount(days.get(i),afterDay);
            if(amount == null){
                amount = "0";
            }
            if(i == days.size()-1){
               dateList = dateList + days.get(i).toString();
               turnoverList = turnoverList + amount;
            }
            else{
                dateList = dateList + days.get(i).toString() + ",";
                turnoverList = turnoverList + amount + ",";
            }
        }
        return new TurnoverReportVO(dateList, turnoverList);
    }

    @Override
    public UserReportVO userStatistics(ReportDTO reportDTO) {
        List<LocalDate> days = getDaysBetween(reportDTO);
        String totalUserList = "";
        String dateList = "";
        String newUserList = "";
        for (int i = 0; i < days.size(); i++) {
            LocalDate afterDay = days.get(i).plusDays(1);
            String newUser = userMapper.sumUser(days.get(i),afterDay);
            String totalUser = userMapper.sumUser(LocalDate.of(0,1,1),afterDay);
            if(i == days.size()-1){
                dateList = dateList + days.get(i).toString();
                newUserList = newUserList + newUser;
                totalUserList = totalUserList + totalUser;
            }
            else{
                dateList = dateList + days.get(i).toString() + ",";
                newUserList = newUserList + newUser + ",";
                totalUserList = totalUserList + totalUser + ",";
            }
        }
        return new UserReportVO(dateList,totalUserList,newUserList);
    }

    @Override
    public OrderReportVO ordersStatistics(ReportDTO reportDTO) {
        List<LocalDate> days = getDaysBetween(reportDTO);
        String dateList = "";
        String orderCountList = "";
        String validOrderCountList = "";
        Integer totalOrderCount = Integer.valueOf(ordersMapper.count(LocalDate.of(0, 1, 1), reportDTO.getEnd().plusDays(1), null));
        Integer validOrderCount = Integer.valueOf(ordersMapper.count(LocalDate.of(0,1,1),reportDTO.getEnd().plusDays(1),Orders.COMPLETED));
        Double orderCompletionRate = 0.0;
        if (totalOrderCount != null && totalOrderCount > 0) {
            orderCompletionRate = new BigDecimal(validOrderCount)
                    .divide(new BigDecimal(totalOrderCount), 2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
        for (int i = 0; i < days.size(); i++) {
            LocalDate afterDay = days.get(i).plusDays(1);
            String order = ordersMapper.count(days.get(i),afterDay,null);
            String validOrder = ordersMapper.count(days.get(i),afterDay, Orders.COMPLETED);
            if(i == days.size()-1){
                dateList = dateList + days.get(i).toString();
                orderCountList = orderCountList + order;
                validOrderCountList = validOrderCountList + validOrder;
            }
            else{
                dateList = dateList + days.get(i).toString() + ",";
                orderCountList = orderCountList + order + ",";
                validOrderCountList = validOrderCountList + validOrder + ",";
            }
        }
        return new OrderReportVO(dateList,orderCountList,validOrderCountList,totalOrderCount,validOrderCount,orderCompletionRate);
    }

    @Override
    public SalesTop10ReportVO top10(ReportDTO reportDTO) {
        List<GoodsSalesDTO> list = orderDetailMapper.top(reportDTO.getBegin(),reportDTO.getEnd());
        String nameList = "";
        String numberList = "";
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i) != null) {
                if (i < 10) {
                    if (i == 9 || i == list.size() - 1) {
                        nameList = nameList + list.get(i).getName();
                        numberList = numberList + list.get(i).getNumber();
                    } else {
                        nameList = nameList + list.get(i).getName() + ",";
                        numberList = numberList + list.get(i).getNumber() + ",";
                    }
                }
            }
        }
        return new SalesTop10ReportVO(nameList,numberList);
    }

    @Override
    public void export(ReportDTO reportDTO, HttpServletResponse response) {
        LocalDate begin = reportDTO.getBegin();
        LocalDate end = reportDTO.getEnd();

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(reportDTO);
        try {
            XSSFWorkbook excel = new XSSFWorkbook(is);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end.plusDays(-1));

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());


            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());


            for (int i = 0; i < 30; i++) {
                ReportDTO reportDay = new ReportDTO(begin.plusDays(i), begin.plusDays(i + 1));

                BusinessDataVO businessData = workspaceService.getBusinessData(reportDay);


                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(reportDay.getBegin().toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }


            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LocalDate> getDaysBetween(ReportDTO reportDTO) {
        List<LocalDate> days = new ArrayList<>();
        LocalDate begin = reportDTO.getBegin();
        LocalDate end = reportDTO.getEnd();

        // 从 begin 开始，循环添加到 end（包含两端日期）
        LocalDate current = begin;
        while (!current.isAfter(end)) {
            days.add(current);
            current = current.plusDays(1);
        }

        return days;
    }
}
