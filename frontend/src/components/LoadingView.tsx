const LoadingView = ({ label }: { label: string }) => {
  return (
    <section className="page page-loading" aria-live="polite">
      <div className="loader" aria-hidden="true" />
      <p>{label}</p>
    </section>
  );
};

export default LoadingView;
