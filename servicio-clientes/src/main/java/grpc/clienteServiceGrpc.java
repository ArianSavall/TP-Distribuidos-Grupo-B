package grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: clientes.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class clienteServiceGrpc {

  private clienteServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "clientes.clienteService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<grpc.ClienteRequest,
      grpc.ClienteResponse> getCrearClienteMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CrearCliente",
      requestType = grpc.ClienteRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteRequest,
      grpc.ClienteResponse> getCrearClienteMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteRequest, grpc.ClienteResponse> getCrearClienteMethod;
    if ((getCrearClienteMethod = clienteServiceGrpc.getCrearClienteMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getCrearClienteMethod = clienteServiceGrpc.getCrearClienteMethod) == null) {
          clienteServiceGrpc.getCrearClienteMethod = getCrearClienteMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CrearCliente"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("CrearCliente"))
              .build();
        }
      }
    }
    return getCrearClienteMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ModificarClienteIdRequest,
      grpc.ClienteResponse> getModificarClienteIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ModificarClienteId",
      requestType = grpc.ModificarClienteIdRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ModificarClienteIdRequest,
      grpc.ClienteResponse> getModificarClienteIdMethod() {
    io.grpc.MethodDescriptor<grpc.ModificarClienteIdRequest, grpc.ClienteResponse> getModificarClienteIdMethod;
    if ((getModificarClienteIdMethod = clienteServiceGrpc.getModificarClienteIdMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getModificarClienteIdMethod = clienteServiceGrpc.getModificarClienteIdMethod) == null) {
          clienteServiceGrpc.getModificarClienteIdMethod = getModificarClienteIdMethod =
              io.grpc.MethodDescriptor.<grpc.ModificarClienteIdRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ModificarClienteId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ModificarClienteIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("ModificarClienteId"))
              .build();
        }
      }
    }
    return getModificarClienteIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ClienteRequest,
      grpc.ClienteResponse> getModificarClienteDniMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ModificarClienteDni",
      requestType = grpc.ClienteRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteRequest,
      grpc.ClienteResponse> getModificarClienteDniMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteRequest, grpc.ClienteResponse> getModificarClienteDniMethod;
    if ((getModificarClienteDniMethod = clienteServiceGrpc.getModificarClienteDniMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getModificarClienteDniMethod = clienteServiceGrpc.getModificarClienteDniMethod) == null) {
          clienteServiceGrpc.getModificarClienteDniMethod = getModificarClienteDniMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ModificarClienteDni"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("ModificarClienteDni"))
              .build();
        }
      }
    }
    return getModificarClienteDniMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ClienteIdRequest,
      grpc.ClienteResponse> getConsultarClienteIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ConsultarClienteId",
      requestType = grpc.ClienteIdRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteIdRequest,
      grpc.ClienteResponse> getConsultarClienteIdMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteIdRequest, grpc.ClienteResponse> getConsultarClienteIdMethod;
    if ((getConsultarClienteIdMethod = clienteServiceGrpc.getConsultarClienteIdMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getConsultarClienteIdMethod = clienteServiceGrpc.getConsultarClienteIdMethod) == null) {
          clienteServiceGrpc.getConsultarClienteIdMethod = getConsultarClienteIdMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteIdRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ConsultarClienteId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("ConsultarClienteId"))
              .build();
        }
      }
    }
    return getConsultarClienteIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ClienteDniRequest,
      grpc.ClienteResponse> getConsultarClienteDniMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ConsultarClienteDni",
      requestType = grpc.ClienteDniRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteDniRequest,
      grpc.ClienteResponse> getConsultarClienteDniMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteDniRequest, grpc.ClienteResponse> getConsultarClienteDniMethod;
    if ((getConsultarClienteDniMethod = clienteServiceGrpc.getConsultarClienteDniMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getConsultarClienteDniMethod = clienteServiceGrpc.getConsultarClienteDniMethod) == null) {
          clienteServiceGrpc.getConsultarClienteDniMethod = getConsultarClienteDniMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteDniRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ConsultarClienteDni"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteDniRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("ConsultarClienteDni"))
              .build();
        }
      }
    }
    return getConsultarClienteDniMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ListarClientesRequest,
      grpc.ListarClientesResponse> getListarClientesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListarClientes",
      requestType = grpc.ListarClientesRequest.class,
      responseType = grpc.ListarClientesResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ListarClientesRequest,
      grpc.ListarClientesResponse> getListarClientesMethod() {
    io.grpc.MethodDescriptor<grpc.ListarClientesRequest, grpc.ListarClientesResponse> getListarClientesMethod;
    if ((getListarClientesMethod = clienteServiceGrpc.getListarClientesMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getListarClientesMethod = clienteServiceGrpc.getListarClientesMethod) == null) {
          clienteServiceGrpc.getListarClientesMethod = getListarClientesMethod =
              io.grpc.MethodDescriptor.<grpc.ListarClientesRequest, grpc.ListarClientesResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListarClientes"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ListarClientesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ListarClientesResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("ListarClientes"))
              .build();
        }
      }
    }
    return getListarClientesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ClienteIdRequest,
      grpc.ClienteResponse> getBorrarClienteIdMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BorrarClienteId",
      requestType = grpc.ClienteIdRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteIdRequest,
      grpc.ClienteResponse> getBorrarClienteIdMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteIdRequest, grpc.ClienteResponse> getBorrarClienteIdMethod;
    if ((getBorrarClienteIdMethod = clienteServiceGrpc.getBorrarClienteIdMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getBorrarClienteIdMethod = clienteServiceGrpc.getBorrarClienteIdMethod) == null) {
          clienteServiceGrpc.getBorrarClienteIdMethod = getBorrarClienteIdMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteIdRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BorrarClienteId"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteIdRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("BorrarClienteId"))
              .build();
        }
      }
    }
    return getBorrarClienteIdMethod;
  }

  private static volatile io.grpc.MethodDescriptor<grpc.ClienteDniRequest,
      grpc.ClienteResponse> getBorrarClienteDniMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "BorrarClienteDni",
      requestType = grpc.ClienteDniRequest.class,
      responseType = grpc.ClienteResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<grpc.ClienteDniRequest,
      grpc.ClienteResponse> getBorrarClienteDniMethod() {
    io.grpc.MethodDescriptor<grpc.ClienteDniRequest, grpc.ClienteResponse> getBorrarClienteDniMethod;
    if ((getBorrarClienteDniMethod = clienteServiceGrpc.getBorrarClienteDniMethod) == null) {
      synchronized (clienteServiceGrpc.class) {
        if ((getBorrarClienteDniMethod = clienteServiceGrpc.getBorrarClienteDniMethod) == null) {
          clienteServiceGrpc.getBorrarClienteDniMethod = getBorrarClienteDniMethod =
              io.grpc.MethodDescriptor.<grpc.ClienteDniRequest, grpc.ClienteResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "BorrarClienteDni"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteDniRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  grpc.ClienteResponse.getDefaultInstance()))
              .setSchemaDescriptor(new clienteServiceMethodDescriptorSupplier("BorrarClienteDni"))
              .build();
        }
      }
    }
    return getBorrarClienteDniMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static clienteServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<clienteServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<clienteServiceStub>() {
        @java.lang.Override
        public clienteServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new clienteServiceStub(channel, callOptions);
        }
      };
    return clienteServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static clienteServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<clienteServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<clienteServiceBlockingStub>() {
        @java.lang.Override
        public clienteServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new clienteServiceBlockingStub(channel, callOptions);
        }
      };
    return clienteServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static clienteServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<clienteServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<clienteServiceFutureStub>() {
        @java.lang.Override
        public clienteServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new clienteServiceFutureStub(channel, callOptions);
        }
      };
    return clienteServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void crearCliente(grpc.ClienteRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCrearClienteMethod(), responseObserver);
    }

    /**
     */
    default void modificarClienteId(grpc.ModificarClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getModificarClienteIdMethod(), responseObserver);
    }

    /**
     */
    default void modificarClienteDni(grpc.ClienteRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getModificarClienteDniMethod(), responseObserver);
    }

    /**
     */
    default void consultarClienteId(grpc.ClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getConsultarClienteIdMethod(), responseObserver);
    }

    /**
     */
    default void consultarClienteDni(grpc.ClienteDniRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getConsultarClienteDniMethod(), responseObserver);
    }

    /**
     */
    default void listarClientes(grpc.ListarClientesRequest request,
        io.grpc.stub.StreamObserver<grpc.ListarClientesResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListarClientesMethod(), responseObserver);
    }

    /**
     */
    default void borrarClienteId(grpc.ClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBorrarClienteIdMethod(), responseObserver);
    }

    /**
     */
    default void borrarClienteDni(grpc.ClienteDniRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getBorrarClienteDniMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service clienteService.
   */
  public static abstract class clienteServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return clienteServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service clienteService.
   */
  public static final class clienteServiceStub
      extends io.grpc.stub.AbstractAsyncStub<clienteServiceStub> {
    private clienteServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected clienteServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new clienteServiceStub(channel, callOptions);
    }

    /**
     */
    public void crearCliente(grpc.ClienteRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCrearClienteMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void modificarClienteId(grpc.ModificarClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getModificarClienteIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void modificarClienteDni(grpc.ClienteRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getModificarClienteDniMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void consultarClienteId(grpc.ClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getConsultarClienteIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void consultarClienteDni(grpc.ClienteDniRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getConsultarClienteDniMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listarClientes(grpc.ListarClientesRequest request,
        io.grpc.stub.StreamObserver<grpc.ListarClientesResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListarClientesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void borrarClienteId(grpc.ClienteIdRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBorrarClienteIdMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void borrarClienteDni(grpc.ClienteDniRequest request,
        io.grpc.stub.StreamObserver<grpc.ClienteResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getBorrarClienteDniMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service clienteService.
   */
  public static final class clienteServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<clienteServiceBlockingStub> {
    private clienteServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected clienteServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new clienteServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public grpc.ClienteResponse crearCliente(grpc.ClienteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCrearClienteMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse modificarClienteId(grpc.ModificarClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getModificarClienteIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse modificarClienteDni(grpc.ClienteRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getModificarClienteDniMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse consultarClienteId(grpc.ClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getConsultarClienteIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse consultarClienteDni(grpc.ClienteDniRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getConsultarClienteDniMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ListarClientesResponse listarClientes(grpc.ListarClientesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListarClientesMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse borrarClienteId(grpc.ClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBorrarClienteIdMethod(), getCallOptions(), request);
    }

    /**
     */
    public grpc.ClienteResponse borrarClienteDni(grpc.ClienteDniRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getBorrarClienteDniMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service clienteService.
   */
  public static final class clienteServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<clienteServiceFutureStub> {
    private clienteServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected clienteServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new clienteServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> crearCliente(
        grpc.ClienteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCrearClienteMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> modificarClienteId(
        grpc.ModificarClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getModificarClienteIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> modificarClienteDni(
        grpc.ClienteRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getModificarClienteDniMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> consultarClienteId(
        grpc.ClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getConsultarClienteIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> consultarClienteDni(
        grpc.ClienteDniRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getConsultarClienteDniMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ListarClientesResponse> listarClientes(
        grpc.ListarClientesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListarClientesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> borrarClienteId(
        grpc.ClienteIdRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBorrarClienteIdMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<grpc.ClienteResponse> borrarClienteDni(
        grpc.ClienteDniRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getBorrarClienteDniMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CREAR_CLIENTE = 0;
  private static final int METHODID_MODIFICAR_CLIENTE_ID = 1;
  private static final int METHODID_MODIFICAR_CLIENTE_DNI = 2;
  private static final int METHODID_CONSULTAR_CLIENTE_ID = 3;
  private static final int METHODID_CONSULTAR_CLIENTE_DNI = 4;
  private static final int METHODID_LISTAR_CLIENTES = 5;
  private static final int METHODID_BORRAR_CLIENTE_ID = 6;
  private static final int METHODID_BORRAR_CLIENTE_DNI = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREAR_CLIENTE:
          serviceImpl.crearCliente((grpc.ClienteRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_MODIFICAR_CLIENTE_ID:
          serviceImpl.modificarClienteId((grpc.ModificarClienteIdRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_MODIFICAR_CLIENTE_DNI:
          serviceImpl.modificarClienteDni((grpc.ClienteRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_CONSULTAR_CLIENTE_ID:
          serviceImpl.consultarClienteId((grpc.ClienteIdRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_CONSULTAR_CLIENTE_DNI:
          serviceImpl.consultarClienteDni((grpc.ClienteDniRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_LISTAR_CLIENTES:
          serviceImpl.listarClientes((grpc.ListarClientesRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ListarClientesResponse>) responseObserver);
          break;
        case METHODID_BORRAR_CLIENTE_ID:
          serviceImpl.borrarClienteId((grpc.ClienteIdRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        case METHODID_BORRAR_CLIENTE_DNI:
          serviceImpl.borrarClienteDni((grpc.ClienteDniRequest) request,
              (io.grpc.stub.StreamObserver<grpc.ClienteResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getCrearClienteMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteRequest,
              grpc.ClienteResponse>(
                service, METHODID_CREAR_CLIENTE)))
        .addMethod(
          getModificarClienteIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ModificarClienteIdRequest,
              grpc.ClienteResponse>(
                service, METHODID_MODIFICAR_CLIENTE_ID)))
        .addMethod(
          getModificarClienteDniMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteRequest,
              grpc.ClienteResponse>(
                service, METHODID_MODIFICAR_CLIENTE_DNI)))
        .addMethod(
          getConsultarClienteIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteIdRequest,
              grpc.ClienteResponse>(
                service, METHODID_CONSULTAR_CLIENTE_ID)))
        .addMethod(
          getConsultarClienteDniMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteDniRequest,
              grpc.ClienteResponse>(
                service, METHODID_CONSULTAR_CLIENTE_DNI)))
        .addMethod(
          getListarClientesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ListarClientesRequest,
              grpc.ListarClientesResponse>(
                service, METHODID_LISTAR_CLIENTES)))
        .addMethod(
          getBorrarClienteIdMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteIdRequest,
              grpc.ClienteResponse>(
                service, METHODID_BORRAR_CLIENTE_ID)))
        .addMethod(
          getBorrarClienteDniMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              grpc.ClienteDniRequest,
              grpc.ClienteResponse>(
                service, METHODID_BORRAR_CLIENTE_DNI)))
        .build();
  }

  private static abstract class clienteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    clienteServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return grpc.ClientesProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("clienteService");
    }
  }

  private static final class clienteServiceFileDescriptorSupplier
      extends clienteServiceBaseDescriptorSupplier {
    clienteServiceFileDescriptorSupplier() {}
  }

  private static final class clienteServiceMethodDescriptorSupplier
      extends clienteServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    clienteServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (clienteServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new clienteServiceFileDescriptorSupplier())
              .addMethod(getCrearClienteMethod())
              .addMethod(getModificarClienteIdMethod())
              .addMethod(getModificarClienteDniMethod())
              .addMethod(getConsultarClienteIdMethod())
              .addMethod(getConsultarClienteDniMethod())
              .addMethod(getListarClientesMethod())
              .addMethod(getBorrarClienteIdMethod())
              .addMethod(getBorrarClienteDniMethod())
              .build();
        }
      }
    }
    return result;
  }
}
