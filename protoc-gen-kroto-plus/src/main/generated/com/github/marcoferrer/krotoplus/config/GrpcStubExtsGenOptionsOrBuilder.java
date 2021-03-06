// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: krotoplus/compiler/config.proto

package com.github.marcoferrer.krotoplus.config;

public interface GrpcStubExtsGenOptionsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:krotoplus.compiler.GrpcStubExtsGenOptions)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Filter used for limiting the input files that are processed by the code generator
   * The default filter will match true against all input files.
   * </pre>
   *
   * <code>.krotoplus.compiler.FileFilter filter = 1;</code>
   */
  boolean hasFilter();
  /**
   * <pre>
   * Filter used for limiting the input files that are processed by the code generator
   * The default filter will match true against all input files.
   * </pre>
   *
   * <code>.krotoplus.compiler.FileFilter filter = 1;</code>
   */
  com.github.marcoferrer.krotoplus.config.FileFilter getFilter();
  /**
   * <pre>
   * Filter used for limiting the input files that are processed by the code generator
   * The default filter will match true against all input files.
   * </pre>
   *
   * <code>.krotoplus.compiler.FileFilter filter = 1;</code>
   */
  com.github.marcoferrer.krotoplus.config.FileFilterOrBuilder getFilterOrBuilder();

  /**
   * <pre>
   * Enable code generation for coroutine supporting service stub extensions.
   * This options generates code that relies on the artifact 'kroto-plus-coroutines'
   * </pre>
   *
   * <code>bool support_coroutines = 2;</code>
   */
  boolean getSupportCoroutines();
}
