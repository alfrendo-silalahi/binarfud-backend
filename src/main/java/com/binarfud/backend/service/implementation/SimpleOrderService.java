package com.binarfud.backend.service.implementation;

import com.binarfud.backend.dto.request.CreateOrderRequest;
import com.binarfud.backend.dto.response.CreateOrderResponse;
import com.binarfud.backend.dto.response.OrderBaseResponse;
import com.binarfud.backend.dto.response.OrderListResponse;
import com.binarfud.backend.exception.ResourceNotFoundException;
import com.binarfud.backend.model.Order;
import com.binarfud.backend.repository.OrderRepository;
import com.binarfud.backend.repository.UserRepository;
import com.binarfud.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimpleOrderService implements OrderService {

    private final UserRepository userRepository;

    private final OrderRepository orderRepository;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest createOrderRequest) {
        var user = userRepository.findById(createOrderRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + createOrderRequest.getUserId() + " not found!"
                ));

        var order = new Order();
        order.setUser(user);
        order.setDestinationAddress(createOrderRequest.getDestinationAddress());
        var newOrder = orderRepository.save(order);

        var createOrderResponse = new CreateOrderResponse();
        createOrderResponse.setCompleted(newOrder.isCompleted());
        createOrderResponse.setOrderId(newOrder.getId());
        createOrderResponse.setUsername(newOrder.getUser().getUsername());
        createOrderResponse.setDestinationAddress(newOrder.getDestinationAddress());
        createOrderResponse.setUserId(newOrder.getUser().getId());

        return createOrderResponse;
    }

    @Override
    public OrderListResponse getOrderList(UUID id, int pageNum, int pageSize, String sortBy, String sortDir) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + id + " not found!"
                ));

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNum - 1, pageSize, sort);
        Page<Order> orderPage = orderRepository.findAllByUser(user, pageable);
        List<Order> orderList = orderPage.getContent();
        List<OrderBaseResponse> orderBaseResponseList = orderList.stream()
                .map(order -> {
                    var orderBaseResponse = new OrderBaseResponse();
                    orderBaseResponse.setCompleted(order.isCompleted());
                    orderBaseResponse.setOrderId(order.getId());
                    orderBaseResponse.setUsername(order.getUser().getUsername());
                    orderBaseResponse.setDestinationAddress(order.getDestinationAddress());
                    orderBaseResponse.setUserId(order.getUser().getId());

                    return orderBaseResponse;
                })
                .toList();

        var orderListResponse = new OrderListResponse();
        orderListResponse.setOrderBaseResponses(orderBaseResponseList);
        orderListResponse.setPageNum(orderPage.getNumber() + 1);
        orderListResponse.setPageSize(orderPage.getSize());
        orderListResponse.setOrderTotalInPage(orderPage.getNumberOfElements());
        orderListResponse.setLastPage(orderPage.isLast());
        orderListResponse.setPageTotal(orderPage.getTotalPages());

        return orderListResponse;
    }

}
