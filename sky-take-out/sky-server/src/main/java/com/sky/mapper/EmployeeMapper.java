package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @AutoFill(value = OperationType.INSERT)
    @Insert("insert into employee(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) " +
            "VALUES (#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime}, #{updateTime},#{createUser},#{updateUser})")
    void save(Employee employee);

    List<Employee> page(EmployeePageQueryDTO employeePageQueryDTO);

    @AutoFill(value = OperationType.UPDATE)
    @Update("update employee set status = #{status} , update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void satstatus(Employee employee);

    @Select("select * from employee where id = #{id}")
    Employee getEmpById(long id);

    @AutoFill(value = OperationType.UPDATE)
    @Update("update employee set name = #{name}, username = #{username} , phone = #{phone}, sex = #{sex}, id_number = #{idNumber}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void update(Employee employee);

    @AutoFill(value = OperationType.UPDATE)
    @Update("update employee set password = #{password} , update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void editPassword(Employee employee);
}
